package com.wanted.backend.domain.identity.infrastructure.redis;

import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class RefreshTokenRedisAdapter implements RefreshTokenRepository {

    private static final String MEMBER_KEY_PREFIX = "auth:{refresh}:member:";
    private static final String TOKEN_KEY_PREFIX = "auth:{refresh}:token:";

    private static final DefaultRedisScript<Long> SAVE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    local previousHash = redis.call('GET', KEYS[1])
                    if (previousHash or '') ~= ARGV[4] then
                        return 0
                    end
                    if previousHash and previousHash ~= ARGV[1] then
                        redis.call('DEL', KEYS[3])
                    end
                    redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[1])
                    redis.call('PSETEX', KEYS[2], ARGV[3], ARGV[2])
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> FIND_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                        return -1
                    end
                    if redis.call('GET', KEYS[2]) ~= ARGV[2] then
                        return -1
                    end
                    local ttl = redis.call('PTTL', KEYS[1])
                    if ttl <= 0 then
                        return -1
                    end
                    return ttl
                    """, Long.class);

    private static final DefaultRedisScript<Long> DELETE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    local tokenHash = redis.call('GET', KEYS[1])
                    if tokenHash and tokenHash == ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        redis.call('DEL', KEYS[2])
                        return 1
                    end
                    return 0
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final int mutationRetryAttempts;

    public RefreshTokenRedisAdapter(
            StringRedisTemplate redisTemplate,
            @Value("${identity.redis.mutation-retry-attempts}") int mutationRetryAttempts
    ) {
        this.redisTemplate = redisTemplate;
        this.mutationRetryAttempts = Math.max(1, mutationRetryAttempts);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = hash(token);
        String memberId = redisTemplate.opsForValue().get(tokenKey(tokenHash));
        if (memberId == null) {
            return Optional.empty();
        }

        Long parsedMemberId;
        try {
            parsedMemberId = Long.valueOf(memberId);
        } catch (NumberFormatException exception) {
            log.error("Invalid member ID stored for refresh token. memberId={}", memberId, exception);
            return Optional.empty();
        }
        Long ttlMillis = redisTemplate.execute(
                FIND_TOKEN_SCRIPT,
                List.of(tokenKey(tokenHash), memberKey(parsedMemberId)),
                memberId,
                tokenHash
        );
        if (ttlMillis == null || ttlMillis <= 0) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        return Optional.of(new RefreshToken(
                null,
                parsedMemberId,
                token,
                now.plus(Duration.ofMillis(ttlMillis)),
                now
        ));
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        Duration ttl = Duration.between(LocalDateTime.now(), refreshToken.getExpiryDate());
        if (ttl.isNegative() || ttl.isZero()) {
            return refreshToken;
        }

        String tokenHash = hash(refreshToken.getToken());
        String memberKey = memberKey(refreshToken.getMemberId());
        for (int attempt = 0; attempt < mutationRetryAttempts; attempt++) {
            String previousTokenHash = redisTemplate.opsForValue().get(memberKey);
            String previousTokenKey = previousTokenHash == null
                    ? memberKey
                    : tokenKey(previousTokenHash);
            Long saved = redisTemplate.execute(
                    SAVE_TOKEN_SCRIPT,
                    List.of(memberKey, tokenKey(tokenHash), previousTokenKey),
                    tokenHash,
                    String.valueOf(refreshToken.getMemberId()),
                    String.valueOf(ttl.toMillis()),
                    previousTokenHash == null ? "" : previousTokenHash
            );
            if (Long.valueOf(1L).equals(saved)) {
                return refreshToken;
            }
        }
        log.error("Failed to save refresh token after all mutation retries. memberId={}",
                refreshToken.getMemberId());
        throw new IllegalStateException("Failed to save refresh token");
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        String memberKey = memberKey(memberId);
        for (int attempt = 0; attempt < mutationRetryAttempts; attempt++) {
            String tokenHash = redisTemplate.opsForValue().get(memberKey);
            if (tokenHash == null) {
                return;
            }
            Long deleted = redisTemplate.execute(
                    DELETE_TOKEN_SCRIPT,
                    List.of(memberKey, tokenKey(tokenHash)),
                    tokenHash
            );
            if (Long.valueOf(1L).equals(deleted)) {
                return;
            }
        }
    }

    private String memberKey(Long memberId) {
        return MEMBER_KEY_PREFIX + memberId;
    }

    private String tokenKey(String tokenHash) {
        return TOKEN_KEY_PREFIX + tokenHash;
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
