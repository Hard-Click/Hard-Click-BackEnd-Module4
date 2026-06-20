package com.wanted.backend.domain.grass.domain.policy;

import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy.MonthlyGrassPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyGrassPeriodPolicyTest {

    private final MonthlyGrassPeriodPolicy policy = new MonthlyGrassPeriodPolicy();

    @Test
    void calculatesCurrentMonthPeriod() {
        MonthlyGrassPeriod result = policy.calculate(
                2026,
                6,
                LocalDate.parse("2026-06-03")
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.parse("2026-06-01"));
        assertThat(result.endDate()).isEqualTo(LocalDate.parse("2026-06-30"));
        assertThat(result.queryEndDate()).isEqualTo(LocalDate.parse("2026-06-03"));
        assertThat(result.queryableDateRange()).isPresent();
        assertThat(result.queryableDateRange().orElseThrow().endDate())
                .isEqualTo(LocalDate.parse("2026-06-03"));
    }

    @Test
    void calculatesFutureMonthWithoutQueryableRange() {
        MonthlyGrassPeriod result = policy.calculate(
                2026,
                7,
                LocalDate.parse("2026-06-03")
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.parse("2026-07-01"));
        assertThat(result.endDate()).isEqualTo(LocalDate.parse("2026-07-31"));
        assertThat(result.queryableDateRange()).isEmpty();
    }

    @Test
    void rejectsMonthOutOfRange() {
        assertThatThrownBy(() -> policy.calculate(
                2026,
                13,
                LocalDate.parse("2026-06-03")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조회 월은 1~12 사이여야 합니다.");
    }
}
