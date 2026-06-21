package com.wanted.backend.domain.grass.infrastructure.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "grass.streak")
public class GrassStreakProperties {

    @Positive
    private int queryPageSize;

    public int queryPageSize() {
        return queryPageSize;
    }

    public void setQueryPageSize(int queryPageSize) {
        this.queryPageSize = queryPageSize;
    }
}
