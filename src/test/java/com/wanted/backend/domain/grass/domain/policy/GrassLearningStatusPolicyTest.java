package com.wanted.backend.domain.grass.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrassLearningStatusPolicyTest {

    private final GrassLearningStatusPolicy policy = new GrassLearningStatusPolicy();

    @Test
    void returnsTrueWhenWatchedLessonExists() {
        assertThat(policy.hasStudyRecord(1, 0)).isTrue();
    }

    @Test
    void returnsTrueWhenStudySecondsExists() {
        assertThat(policy.hasStudyRecord(0, 1)).isTrue();
    }

    @Test
    void returnsFalseWhenNoLearningActivityExists() {
        assertThat(policy.hasStudyRecord(0, 0)).isFalse();
    }
}
