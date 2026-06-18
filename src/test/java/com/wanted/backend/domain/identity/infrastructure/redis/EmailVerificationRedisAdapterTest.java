package com.wanted.backend.domain.identity.infrastructure.redis;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationRedisAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private EmailVerificationRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmailVerificationRedisAdapter(
                redisTemplate,
                Duration.ofMinutes(30),
                Duration.ofSeconds(30),
                3
        );
    }

    @Test
    void reservesTokenWithExpiringProcessingLease() {
        String token = "verification-token";
        String tokenKey = "email:{verification}:token:SIGNUP:" + hash(token);
        String verificationKey = "email:{verification}:record:SIGNUP:user@example.com";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(valueOperations.get(tokenKey)).thenReturn(verificationKey);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, verificationKey)),
                any(Object[].class)
        )).thenReturn(1L);
        when(hashOperations.entries(verificationKey)).thenReturn(Map.of(
                "email", "user@example.com",
                "code", hash("123456"),
                "purpose", "SIGNUP",
                "status", "PROCESSING",
                "token", token,
                "expiresAt", expiresAt.toString(),
                "verifiedAt", LocalDateTime.now().toString()
        ));

        EmailVerification reserved = adapter.reserveValidToken(
                "USER@example.com",
                token,
                EmailPurpose.SIGNUP,
                "reservation-id"
        ).orElseThrow();

        assertThat(reserved.getStatus().name()).isEqualTo("PROCESSING");
        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, verificationKey)),
                eq("user@example.com"),
                eq("SIGNUP"),
                eq("reservation-id"),
                any(String.class),
                any(String.class)
        );
    }

    @Test
    void returnsEmptyWhenTokenKeyDoesNotExist() {
        String token = "missing-token";
        String tokenKey = "email:{verification}:token:SIGNUP:" + hash(token);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn(null);

        assertThat(adapter.reserveValidToken(
                "user@example.com",
                token,
                EmailPurpose.SIGNUP,
                "reservation-id"
        )).isEmpty();
    }

    @Test
    void returnsEmptyWhenProcessingReservationLeaseIsStillActive() {
        assertAtomicReservationRejection("processing-token");
    }

    @Test
    void returnsEmptyWhenEmailOrPurposeDoesNotMatch() {
        assertAtomicReservationRejection("mismatched-token");
    }

    @Test
    void returnsEmptyWhenAtomicReservationFails() {
        assertAtomicReservationRejection("failed-token");
    }

    @Test
    void savesPendingVerificationAtomicallyWithoutPlainCode() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        String rawCode = verification.getCode();
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                any(List.class),
                any(Object[].class)
        )).thenReturn(1L);

        adapter.save(verification);

        ArgumentCaptor<Object[]> arguments = ArgumentCaptor.forClass(Object[].class);
        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(
                        "email:{verification}:record:SIGNUP:user@example.com",
                        "email:{verification}:record:SIGNUP:user@example.com",
                        "email:{verification}:record:SIGNUP:user@example.com"
                )),
                arguments.capture()
        );
        assertThat(arguments.getValue()[1]).isEqualTo(hash(rawCode));
        assertThat(arguments.getValue()[1]).isNotEqualTo(rawCode);
        assertThat(arguments.getValue()[1].toString()).hasSize(64);
    }

    @Test
    void savesVerifiedTokenIndexInSameClusterSlotWithSeparateTtl() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );
        verification.verify(verification.getCode());
        String token = verification.getVerificationToken();
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                any(List.class),
                any(Object[].class)
        )).thenReturn(1L);

        adapter.save(verification);

        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(
                        "email:{verification}:record:SIGNUP:user@example.com",
                        "email:{verification}:token:SIGNUP:" + hash(token),
                        "email:{verification}:record:SIGNUP:user@example.com"
                )),
                eq("user@example.com"),
                eq(verification.getCodeHash()),
                eq("SIGNUP"),
                eq("VERIFIED"),
                eq(token),
                any(String.class),
                any(String.class),
                eq(hash(token)),
                eq(String.valueOf(Duration.ofMinutes(30).toMillis())),
                eq("1"),
                eq("")
        );
    }

    @Test
    void luaScriptsNeverUseArgumentsAsRedisKeys() throws IllegalAccessException {
        for (Field field : EmailVerificationRedisAdapter.class.getDeclaredFields()) {
            if (!RedisScript.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            RedisScript<?> script = (RedisScript<?>) field.get(null);
            assertThat(script.getScriptAsString())
                    .doesNotMatch("(?s).*redis\\.call\\('[A-Za-z]+',\\s*ARGV\\[.*");
        }
    }

    private void assertAtomicReservationRejection(String token) {
        String tokenKey = "email:{verification}:token:SIGNUP:" + hash(token);
        String verificationKey = "email:{verification}:record:SIGNUP:user@example.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn(verificationKey);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, verificationKey)),
                any(Object[].class)
        )).thenReturn(0L);

        assertThat(adapter.reserveValidToken(
                "user@example.com",
                token,
                EmailPurpose.SIGNUP,
                "reservation-id"
        )).isEmpty();
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
