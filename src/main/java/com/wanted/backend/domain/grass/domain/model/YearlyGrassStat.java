package com.wanted.backend.domain.grass.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public record YearlyGrassStat(
        Long memberId,
        LocalDate statDate,
        Integer watchedLessonCount
) {
    public YearlyGrassStat {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (statDate == null) {
            throw new IllegalArgumentException("통계 날짜는 필수입니다.");
        }
        if (watchedLessonCount == null) {
            throw new IllegalArgumentException("수강량은 필수입니다.");
        }
    }

    public YearlyGrassStat merge(YearlyGrassStat other) {
        if (other == null) {
            throw new IllegalArgumentException("합산할 연간 잔디 통계는 필수입니다.");
        }
        if (!Objects.equals(memberId, other.memberId)) {
            throw new IllegalArgumentException("서로 다른 회원의 연간 잔디 통계는 합산할 수 없습니다.");
        }
        if (!Objects.equals(statDate, other.statDate)) {
            throw new IllegalArgumentException("서로 다른 날짜의 연간 잔디 통계는 합산할 수 없습니다.");
        }
        if (watchedLessonCount == null || other.watchedLessonCount == null) {
            throw new IllegalArgumentException("수강량은 필수입니다.");
        }

        return new YearlyGrassStat(
                memberId,
                statDate,
                watchedLessonCount + other.watchedLessonCount
        );
    }
}
