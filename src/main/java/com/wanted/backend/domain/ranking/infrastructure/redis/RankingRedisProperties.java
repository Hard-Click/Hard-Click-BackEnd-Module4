package com.wanted.backend.domain.ranking.infrastructure.redis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "ranking.redis")
public class RankingRedisProperties {

    @NotBlank
    private String keyPrefix;

    @Positive
    private long defaultLimit;

    public String keyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long defaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(long defaultLimit) {
        this.defaultLimit = defaultLimit;
    }
}
