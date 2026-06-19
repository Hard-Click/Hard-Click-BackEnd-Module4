package com.wanted.backend.domain.grass.domain.policy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudyTimeGrassLevelPolicyTest {

    @Test
    void calculatesLevelByThresholdSeconds() {
        StudyTimeGrassLevelPolicy policy = new StudyTimeGrassLevelPolicy(List.of(1, 1800, 3600, 7200));

        assertThat(policy.calculate(0)).isZero();
        assertThat(policy.calculate(1)).isEqualTo(1);
        assertThat(policy.calculate(1799)).isEqualTo(1);
        assertThat(policy.calculate(1800)).isEqualTo(2);
        assertThat(policy.calculate(3600)).isEqualTo(3);
        assertThat(policy.calculate(7200)).isEqualTo(4);
        assertThat(policy.calculate(10000)).isEqualTo(4);
    }

    @Test
    void rejectsInvalidThresholds() {
        assertThatThrownBy(() -> new StudyTimeGrassLevelPolicy(List.of(1, 3600, 1800)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("순공시간 잔디 레벨 기준은 오름차순이어야 합니다.");
    }

    @Test
    void rejectsNullThresholds() {
        assertThatThrownBy(() -> new StudyTimeGrassLevelPolicy(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyThresholds() {
        assertThatThrownBy(() -> new StudyTimeGrassLevelPolicy(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullThresholdValue() {
        assertThatThrownBy(() -> new StudyTimeGrassLevelPolicy(java.util.Arrays.asList(1, null, 3600)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroThresholdValue() {
        assertThatThrownBy(() -> new StudyTimeGrassLevelPolicy(List.of(1, 0, 3600)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeStudySeconds() {
        StudyTimeGrassLevelPolicy policy = new StudyTimeGrassLevelPolicy(List.of(1, 1800, 3600, 7200));

        assertThatThrownBy(() -> policy.calculate(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("순공시간은 0 이상이어야 합니다.");
    }
}
