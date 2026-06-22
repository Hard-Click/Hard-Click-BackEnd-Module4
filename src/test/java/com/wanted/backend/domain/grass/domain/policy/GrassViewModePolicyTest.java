package com.wanted.backend.domain.grass.domain.policy;

import com.wanted.backend.domain.grass.domain.model.GrassViewMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrassViewModePolicyTest {

    private final GrassViewModePolicy policy = new GrassViewModePolicy();

    @Test
    void resolvesMonthlyViewMode() {
        assertThat(policy.resolve("monthly")).isEqualTo(GrassViewMode.MONTHLY);
    }

    @Test
    void resolvesYearlyViewMode() {
        assertThat(policy.resolve("yearly")).isEqualTo(GrassViewMode.YEARLY);
    }

    @Test
    void rejectsInvalidViewMode() {
        assertThatThrownBy(() -> policy.resolve("weekly"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔디 보기 모드는 monthly 또는 yearly여야 합니다.");
    }

    @Test
    void rejectsBlankViewMode() {
        assertThatThrownBy(() -> policy.resolve(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잔디 보기 모드는 필수입니다.");
    }

    @Test
    void requiresMonthForMonthlyView() {
        assertThatThrownBy(() -> policy.requireMonthForMonthly(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월별 잔디 조회 시 month는 필수입니다.");
    }

    @Test
    void rejectsMonthlyViewWhenMonthIsLessThanOne() {
        assertThatThrownBy(() -> policy.requireMonthForMonthly(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월별 잔디 조회 시 month는 1~12여야 합니다.");
    }

    @Test
    void rejectsMonthlyViewWhenMonthIsGreaterThanTwelve() {
        assertThatThrownBy(() -> policy.requireMonthForMonthly(13))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월별 잔디 조회 시 month는 1~12여야 합니다.");
    }
}
