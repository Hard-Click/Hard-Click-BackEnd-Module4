package com.wanted.backend.domain.identity.infrastructure.redis;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.VerificationStatus;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class EmailVerificationRedisAdapter implements EmailVerificationRepository {

    private static final String VERIFICATION_KEY_PREFIX = "email:{verification}:record:";
    private static final String TOKEN_KEY_PREFIX = "email:{verification}:token:";
    private static final String SEND_COUNT_KEY_PREFIX = "email:{send-count}:";
    private static final ZoneId DAILY_LIMIT_ZONE = ZoneId.of("Asia/Seoul");

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_PURPOSE = "purpose";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_EXPIRES_AT = "expiresAt";
    private static final String FIELD_VERIFIED_AT = "verifiedAt";

    private static final DefaultRedisScript<Long> SAVE_VERIFICATION_SCRIPT =
            new DefaultRedisScript<>("""
                    local previousTokenHash = redis.call('HGET', KEYS[1], 'tokenHash')
                    local expectedPreviousTokenHash = ARGV[11]
                    if (previousTokenHash or '') ~= expectedPreviousTokenHash then
                        return 0
                    end
                    if previousTokenHash and previousTokenHash ~= '' and previousTokenHash ~= ARGV[8] then
                        redis.call('DEL', KEYS[3])
                    end
                    redis.call('HSET', KEYS[1],
                        'email', ARGV[1],
                        'code', ARGV[2],
                        'purpose', ARGV[3],
                        'status', ARGV[4],
                        'token', ARGV[5],
                        'expiresAt', ARGV[6],
                        'verifiedAt', ARGV[7],
                        'tokenHash', ARGV[8])
                    redis.call('HDEL', KEYS[1], 'reservationId', 'reservationExpiresAt')
                    redis.call('PEXPIRE', KEYS[1], ARGV[9])
                    if ARGV[10] == '1' then
                        redis.call('PSETEX', KEYS[2], ARGV[9], KEYS[1])
                    elseif ARGV[8] ~= '' then
                        redis.call('DEL', KEYS[2])
                    end
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> RESERVE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) ~= KEYS[2] then
                        return 0
                    end
                    local status = redis.call('HGET', KEYS[2], 'status')
                    if status == 'PROCESSING' then
                        local reservationExpiresAt = tonumber(redis.call('HGET', KEYS[2], 'reservationExpiresAt') or '0')
                        if reservationExpiresAt > tonumber(ARGV[4]) then
                            return 0
                        end
                    elseif status ~= 'VERIFIED' then
                        return 0
                    end
                    if string.lower(redis.call('HGET', KEYS[2], 'email')) ~= ARGV[1] then
                        return 0
                    end
                    if redis.call('HGET', KEYS[2], 'purpose') ~= ARGV[2] then
                        return 0
                    end
                    redis.call('HSET', KEYS[2],
                        'status', 'PROCESSING',
                        'reservationId', ARGV[3],
                        'reservationExpiresAt', ARGV[5])
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> COMPLETE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) ~= KEYS[2] then
                        return 0
                    end
                    local status = redis.call('HGET', KEYS[2], 'status')
                    local reservationId = redis.call('HGET', KEYS[2], 'reservationId')
                    if status == 'USED' and reservationId == ARGV[1] then
                        return 1
                    end
                    if status ~= 'PROCESSING' then
                        return 0
                    end
                    if reservationId ~= ARGV[1] then
                        return 0
                    end
                    redis.call('HSET', KEYS[2], 'status', 'USED')
                    redis.call('HDEL', KEYS[2], 'reservationExpiresAt')
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) ~= KEYS[2] then
                        return 0
                    end
                    if redis.call('HGET', KEYS[2], 'status') ~= 'PROCESSING' then
                        return 0
                    end
                    if redis.call('HGET', KEYS[2], 'reservationId') ~= ARGV[1] then
                        return 0
                    end
                    redis.call('HSET', KEYS[2], 'status', 'VERIFIED')
                    redis.call('HDEL', KEYS[2], 'reservationId', 'reservationExpiresAt')
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> ACQUIRE_SEND_PERMISSION_SCRIPT =
            new DefaultRedisScript<>("""
                    local count = redis.call('INCR', KEYS[1])
                    if count == 1 then
                        redis.call('PEXPIREAT', KEYS[1], ARGV[2])
                    end
                    if count > tonumber(ARGV[1]) then
                        redis.call('DECR', KEYS[1])
                        return 0
                    end
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> REVOKE_VERIFICATION_SCRIPT =
            new DefaultRedisScript<>("""
                    local tokenHash = redis.call('HGET', KEYS[1], 'tokenHash')
                    if tokenHash and tokenHash ~= '' and tokenHash ~= ARGV[1] then
                        return 0
                    end
                    redis.call('DEL', KEYS[1])
                    if tokenHash and tokenHash ~= '' and tokenHash == ARGV[1] then
                        redis.call('DEL', KEYS[2])
                    end
                    return 1
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final Duration verifiedTokenTtl;
    private final Duration reservationTtl;
    private final int mutationRetryAttempts;

    public EmailVerificationRedisAdapter(
            StringRedisTemplate redisTemplate,
            @Value("${identity.email.verified-token-ttl}") Duration verifiedTokenTtl,
            @Value("${identity.email.processing-reservation-ttl}") Duration reservationTtl,
            @Value("${identity.redis.mutation-retry-attempts}") int mutationRetryAttempts
    ) {
        this.redisTemplate = redisTemplate;
        this.verifiedTokenTtl = verifiedTokenTtl;
        this.reservationTtl = reservationTtl;
        this.mutationRetryAttempts = Math.max(1, mutationRetryAttempts);
    }

    @Override
    public void save(EmailVerification verification) {
        String verificationKey = verificationKey(verification.getEmail(), verification.getPurpose());
        LocalDateTime now = LocalDateTime.now();
        Duration ttl = verification.getStatus() == VerificationStatus.VERIFIED
                ? verifiedTokenTtl
                : Duration.between(now, verification.getExpiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            revokeActiveByEmailAndPurpose(verification.getEmail(), verification.getPurpose());
            return;
        }

        LocalDateTime effectiveExpiresAt = verification.getStatus() == VerificationStatus.VERIFIED
                ? now.plus(ttl)
                : verification.getExpiresAt();
        String token = blankIfNull(verification.getVerificationToken());
        String tokenHash = token.isBlank() ? "" : hash(token);
        String tokenKey = token.isBlank() ? verificationKey : tokenKey(verification.getPurpose(), token);
        for (int attempt = 0; attempt < mutationRetryAttempts; attempt++) {
            Object previousTokenHashValue = redisTemplate.opsForHash().get(verificationKey, "tokenHash");
            String previousTokenHash = previousTokenHashValue == null ? "" : String.valueOf(previousTokenHashValue);
            String previousTokenKey = previousTokenHash.isBlank()
                    ? verificationKey
                    : tokenKeyByHash(verification.getPurpose(), previousTokenHash);

            Long saved = redisTemplate.execute(
                    SAVE_VERIFICATION_SCRIPT,
                    List.of(verificationKey, tokenKey, previousTokenKey),
                    verification.getEmail(),
                    verification.getCodeHash(),
                    verification.getPurpose().name(),
                    verification.getStatus().name(),
                    token,
                    effectiveExpiresAt.toString(),
                    blankIfNull(verification.getVerifiedAt()),
                    tokenHash,
                    String.valueOf(ttl.toMillis()),
                    verification.getStatus() == VerificationStatus.VERIFIED && !token.isBlank() ? "1" : "0",
                    previousTokenHash
            );
            if (Long.valueOf(1L).equals(saved)) {
                return;
            }
        }
        log.error("Failed to save email verification after all mutation retries. email={}, purpose={}",
                verification.getEmail(), verification.getPurpose());
        throw new IllegalStateException("Failed to save email verification");
    }

    @Override
    public Optional<EmailVerification> findLatestPendingByEmailAndPurpose(String email, EmailPurpose purpose) {
        return findByVerificationKey(verificationKey(email, purpose))
                .filter(EmailVerification::isPending);
    }

    @Override
    public Optional<EmailVerification> findByVerificationTokenAndPurpose(String token, EmailPurpose purpose) {
        String verificationKey = redisTemplate.opsForValue().get(tokenKey(purpose, token));
        if (verificationKey == null) {
            return Optional.empty();
        }
        return findByVerificationKey(verificationKey)
                .filter(verification -> token.equals(verification.getVerificationToken()));
    }

    @Override
    public Optional<EmailVerification> reserveValidToken(
            String email,
            String token,
            EmailPurpose purpose,
            String reservationId
    ) {
        String tokenKey = tokenKey(purpose, token);
        String verificationKey = redisTemplate.opsForValue().get(tokenKey);
        if (verificationKey == null) {
            return Optional.empty();
        }

        long now = System.currentTimeMillis();
        Long reserved = redisTemplate.execute(
                RESERVE_TOKEN_SCRIPT,
                List.of(tokenKey, verificationKey),
                normalizeEmail(email),
                purpose.name(),
                reservationId,
                String.valueOf(now),
                String.valueOf(now + reservationTtl.toMillis())
        );
        if (!Long.valueOf(1L).equals(reserved)) {
            return Optional.empty();
        }
        return findByVerificationKey(verificationKey);
    }

    @Override
    public boolean completeTokenConsumption(String token, EmailPurpose purpose, String reservationId) {
        String tokenKey = tokenKey(purpose, token);
        String verificationKey = redisTemplate.opsForValue().get(tokenKey);
        if (verificationKey == null) {
            return false;
        }
        Long completed = redisTemplate.execute(
                COMPLETE_TOKEN_SCRIPT,
                List.of(tokenKey, verificationKey),
                reservationId
        );
        return Long.valueOf(1L).equals(completed);
    }

    @Override
    public void releaseTokenReservation(String token, EmailPurpose purpose, String reservationId) {
        String tokenKey = tokenKey(purpose, token);
        String verificationKey = redisTemplate.opsForValue().get(tokenKey);
        if (verificationKey == null) {
            return;
        }
        redisTemplate.execute(
                RELEASE_TOKEN_SCRIPT,
                List.of(tokenKey, verificationKey),
                reservationId
        );
    }

    @Override
    public boolean tryAcquireSendPermission(
            String email,
            EmailPurpose purpose,
            int dailyLimit,
            LocalDateTime expiresAt
    ) {
        Long acquired = redisTemplate.execute(
                ACQUIRE_SEND_PERMISSION_SCRIPT,
                List.of(sendCountKey(email, purpose, LocalDate.now(DAILY_LIMIT_ZONE))),
                String.valueOf(dailyLimit),
                String.valueOf(toEpochMilli(expiresAt))
        );
        return Long.valueOf(1L).equals(acquired);
    }

    @Override
    public void revokeActiveByEmailAndPurpose(String email, EmailPurpose purpose) {
        String verificationKey = verificationKey(email, purpose);
        for (int attempt = 0; attempt < mutationRetryAttempts; attempt++) {
            Object tokenHashValue = redisTemplate.opsForHash().get(verificationKey, "tokenHash");
            String tokenHash = tokenHashValue == null ? "" : String.valueOf(tokenHashValue);
            String tokenKey = tokenHash.isBlank() ? verificationKey : tokenKeyByHash(purpose, tokenHash);
            Long revoked = redisTemplate.execute(
                    REVOKE_VERIFICATION_SCRIPT,
                    List.of(verificationKey, tokenKey),
                    tokenHash
            );
            if (Long.valueOf(1L).equals(revoked)) {
                return;
            }
        }
        log.warn("Failed to revoke active email verification after all mutation retries. email={}, purpose={}",
                email, purpose);
    }

    private Optional<EmailVerification> findByVerificationKey(String key) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(EmailVerification.restore(
                null,
                requiredValue(values, FIELD_EMAIL),
                requiredValue(values, FIELD_CODE),
                EmailPurpose.valueOf(requiredValue(values, FIELD_PURPOSE)),
                VerificationStatus.valueOf(requiredValue(values, FIELD_STATUS)),
                nullableValue(requiredValue(values, FIELD_TOKEN)),
                LocalDateTime.parse(requiredValue(values, FIELD_EXPIRES_AT)),
                nullableDateTime(requiredValue(values, FIELD_VERIFIED_AT))
        ));
    }

    private String verificationKey(String email, EmailPurpose purpose) {
        return VERIFICATION_KEY_PREFIX + purpose.name() + ":" + normalizeEmail(email);
    }

    private String tokenKey(EmailPurpose purpose, String token) {
        return tokenKeyByHash(purpose, hash(token));
    }

    private String tokenKeyByHash(EmailPurpose purpose, String tokenHash) {
        return TOKEN_KEY_PREFIX + purpose.name() + ":" + tokenHash;
    }

    private String sendCountKey(String email, EmailPurpose purpose, LocalDate date) {
        return SEND_COUNT_KEY_PREFIX + purpose.name() + ":" + normalizeEmail(email) + ":" + date;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String requiredValue(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        if (value == null) {
            throw new IllegalStateException("Missing Redis hash field: " + field);
        }
        return String.valueOf(value);
    }

    private String nullableValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return String.valueOf(value);
    }

    private String blankIfNull(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private LocalDateTime nullableDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(DAILY_LIMIT_ZONE).toInstant().toEpochMilli();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
