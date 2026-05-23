package com.wanted.backend.domain.learning_activity.application.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoCompletionPolicyTest {

    private final VideoCompletionPolicy policy = new VideoCompletionPolicy();

    @Test
    void canCompleteWhenWatchTimeIsAtLeastNinetyPercent() {
        assertThat(policy.canComplete(270, 300)).isTrue();
        assertThat(policy.canComplete(271, 300)).isTrue();
    }

    @Test
    void cannotCompleteWhenWatchTimeIsLessThanNinetyPercent() {
        assertThat(policy.canComplete(269, 300)).isFalse();
    }

    @Test
    void cannotCompleteWhenDurationIsInvalid() {
        assertThat(policy.canComplete(10, 0)).isFalse();
        assertThat(policy.canComplete(10, null)).isFalse();
        assertThat(policy.canComplete(null, 300)).isFalse();
    }
}
