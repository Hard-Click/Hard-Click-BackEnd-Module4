package com.wanted.backend.domain.study_timer.infrastructure.lock;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "study-timer.member-lock")
public class StudyTimerMemberLockProperties {

    @Positive
    private int timeoutMilliseconds;

    public int timeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    public void setTimeoutMilliseconds(int timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }
}
