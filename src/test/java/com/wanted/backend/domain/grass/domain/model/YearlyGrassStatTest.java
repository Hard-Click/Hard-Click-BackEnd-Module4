package com.wanted.backend.domain.grass.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearlyGrassStatTest {

    @Test
    void mergesSameMemberAndDateStats() {
        YearlyGrassStat left = new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 1);
        YearlyGrassStat right = new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 2);

        YearlyGrassStat result = left.merge(right);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.statDate()).isEqualTo(LocalDate.parse("2026-01-01"));
        assertThat(result.watchedLessonCount()).isEqualTo(3);
    }

    @Test
    void rejectsDifferentMemberStats() {
        YearlyGrassStat left = new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 1);
        YearlyGrassStat right = new YearlyGrassStat(2L, LocalDate.parse("2026-01-01"), 2);

        assertThatThrownBy(() -> left.merge(right))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("서로 다른 회원의 연간 잔디 통계는 합산할 수 없습니다.");
    }

    @Test
    void rejectsDifferentDateStats() {
        YearlyGrassStat left = new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 1);
        YearlyGrassStat right = new YearlyGrassStat(1L, LocalDate.parse("2026-01-02"), 2);

        assertThatThrownBy(() -> left.merge(right))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("서로 다른 날짜의 연간 잔디 통계는 합산할 수 없습니다.");
    }

    @Test
    void rejectsNullMergeTarget() {
        YearlyGrassStat left = new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 1);

        assertThatThrownBy(() -> left.merge(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("합산할 연간 잔디 통계는 필수입니다.");
    }

    @Test
    void rejectsNullMemberId() {
        assertThatThrownBy(() -> new YearlyGrassStat(null, LocalDate.parse("2026-01-01"), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");
    }

    @Test
    void rejectsNullStatDate() {
        assertThatThrownBy(() -> new YearlyGrassStat(1L, null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("통계 날짜는 필수입니다.");
    }

    @Test
    void rejectsNullWatchedLessonCount() {
        assertThatThrownBy(() -> new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수강량은 필수입니다.");
    }
}
