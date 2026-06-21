package com.wanted.backend.domain.grass.infrastructure.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Component
@Validated
@ConfigurationProperties(prefix = "grass.study-time")
public class GrassStudyTimeProperties {

    @NotEmpty
    private List<@Positive Integer> levelThresholdSeconds = new ArrayList<>();

    public List<Integer> levelThresholdSeconds() {
        return List.copyOf(levelThresholdSeconds);
    }

    public void setLevelThresholdSeconds(List<Integer> levelThresholdSeconds) {
        this.levelThresholdSeconds = List.copyOf(levelThresholdSeconds);
    }
}
