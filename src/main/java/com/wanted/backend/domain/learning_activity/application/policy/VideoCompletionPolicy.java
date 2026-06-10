package com.wanted.backend.domain.learning_activity.application.policy;

import org.springframework.stereotype.Component;

@Component
public class VideoCompletionPolicy {

    private static final double COMPLETION_THRESHOLD = 0.9;

    public boolean canComplete(Integer watchTimeSec, Integer durationSeconds) {
        if (watchTimeSec == null || durationSeconds == null || durationSeconds <= 0) {
            return false;
        }

        return watchTimeSec >= Math.ceil(durationSeconds * COMPLETION_THRESHOLD);
    }
}
