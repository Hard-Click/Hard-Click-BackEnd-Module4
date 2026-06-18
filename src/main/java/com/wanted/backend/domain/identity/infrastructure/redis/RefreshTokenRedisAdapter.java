package com.wanted.backend.domain.identity.infrastructure.redis;

import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRedisAdapter implements RefreshTokenRepository {

    private static final String MEMBER_KEY_PREFIX = "auth:{refresh}:member:";
    private static final String TOKEN_KEY_PREFIX = "auth:{refresh}:token:";

    private static final DefaultRedisScript<Long> SAVE_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    local previousHash = redis.call('GET', KEYS[1])
                    if previousHash and previousHash == ARGV[4] then
                        redis.call('DEL', KEYS[3])
                    end
                    redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[1])
                    redis.call('PSETEX', KEYS[2], ARGV[3], ARGV[2])
                    return 1
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

        Long parsedMemberId = Long.valueOf(memberId);
        String storedHash = redisTemplate.opsForValue().get(memberKey(parsedMemberId));
        if (!tokenHash.equals(storedHash)) {
            return Optional.empty();
        }

        Long ttlSeconds = redisTemplate.getExpire(tokenKey(tokenHash));
        if (ttlSeconds == null || ttlSeconds <= 0) {
            deleteByMemberId(parsedMemberId);
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        return Optional.of(new RefreshToken(
                null,
                parsedMemberId,
                token,
                now.plusSeconds(ttlSeconds),
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
        String previousTokenHash = redisTemplate.opsForValue().get(memberKey);
        String previousTokenKey = previousTokenHash == null
                ? memberKey
                : tokenKey(previousTokenHash);
        redisTemplate.execute(
                SAVE_TOKEN_SCRIPT,
                List.of(memberKey, tokenKey(tokenHash), previousTokenKey),
                tokenHash,
                String.valueOf(refreshToken.getMemberId()),
                String.valueOf(ttl.toMillis()),
                previousTokenHash == null ? "" : previousTokenHash
        );
        return refreshToken;
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
