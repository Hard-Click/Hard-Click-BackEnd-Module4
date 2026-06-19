package com.wanted.backend.domain.identity.infrastructure.redis;

import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRedisAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RefreshTokenRedisAdapter(redisTemplate, 3);
    }

    @Test
    void savesRefreshTokenAsHashWithTtl() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = new RefreshToken(
                null,
                1L,
                "refresh-token",
                now.plusDays(14),
                now
        );
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                any(List.class),
                any(Object[].class)
        )).thenReturn(1L);
        adapter.save(refreshToken);

        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(
                        "auth:{refresh}:member:1",
                        "auth:{refresh}:token:" + hash("refresh-token"),
                        "auth:{refresh}:member:1"
                )),
                eq(hash("refresh-token")),
                eq("1"),
                any(String.class),
                eq("")
        );
    }

    @Test
    void findsRefreshTokenThroughHashedTokenKey() {
        String token = "refresh-token";
        String tokenHash = hash(token);
        String tokenKey = "auth:{refresh}:token:" + tokenHash;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn("1");
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, "auth:{refresh}:member:1")),
                eq("1"),
                eq(tokenHash)
        )).thenReturn(120_000L);

        RefreshToken found = adapter.findByToken(token).orElseThrow();

        assertThat(found.getMemberId()).isEqualTo(1L);
        assertThat(found.getToken()).isEqualTo(token);
        assertThat(found.isExpired()).isFalse();
    }

    @Test
    void returnsEmptyWhenStoredTokenHashDoesNotMatch() {
        String token = "refresh-token";
        String tokenHash = hash(token);
        String tokenKey = "auth:{refresh}:token:" + tokenHash;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn("1");
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, "auth:{refresh}:member:1")),
                eq("1"),
                eq(tokenHash)
        )).thenReturn(-1L);

        assertThat(adapter.findByToken(token)).isEmpty();
    }

    @Test
    void returnsEmptyWithoutDeletingWhenTokenTtlIsNotPositive() {
        String token = "refresh-token";
        String tokenHash = hash(token);
        String tokenKey = "auth:{refresh}:token:" + tokenHash;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn("1");
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of(tokenKey, "auth:{refresh}:member:1")),
                eq("1"),
                eq(tokenHash)
        )).thenReturn(-1L);

        assertThat(adapter.findByToken(token)).isEmpty();
        verify(valueOperations, never()).get("auth:{refresh}:member:1");
    }

    @Test
    void returnsEmptyWhenStoredMemberIdIsInvalid() {
        String token = "refresh-token";
        String tokenKey = "auth:{refresh}:token:" + hash(token);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tokenKey)).thenReturn("invalid-member-id");

        assertThat(adapter.findByToken(token)).isEmpty();
    }

    @Test
    void throwsWhenRefreshTokenSaveRetriesAreExhausted() {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = new RefreshToken(
                null,
                1L,
                "refresh-token",
                now.plusDays(14),
                now
        );
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                any(List.class),
                any(Object[].class)
        )).thenReturn(0L);

        assertThatThrownBy(() -> adapter.save(refreshToken))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void luaScriptsNeverUseArgumentsAsRedisKeys() throws IllegalAccessException {
        for (Field field : RefreshTokenRedisAdapter.class.getDeclaredFields()) {
            if (!RedisScript.class.isAssignableFrom(field.getType())) {
                continue;
            }
            assertThat(Modifier.isStatic(field.getModifiers()))
                    .as("RedisScript field %s must remain static", field.getName())
                    .isTrue();
            field.setAccessible(true);
            RedisScript<?> script = (RedisScript<?>) field.get(null);
            assertThat(script.getScriptAsString())
                    .doesNotMatch("(?s).*redis\\.call\\('[A-Za-z]+',\\s*ARGV\\[.*");
        }
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
