package com.wanted.backend.domain.stats.domain.model;

import java.time.LocalDate;

public record DailyStudyStat(
        Long memberId,
        LocalDate statDate,
        Integer watchedLessonCount,
        Integer studySeconds,
        Integer completedLessonCount
) {

    public DailyStudyStat {
        validate(memberId, statDate, watchedLessonCount, studySeconds, completedLessonCount);
    }

    public static DailyStudyStat empty(Long memberId, LocalDate statDate) {
        return new DailyStudyStat(memberId, statDate, 0, 0, 0);
    }

    private static void validate(
            Long memberId,
            LocalDate statDate,
            Integer watchedLessonCount,
            Integer studySeconds,
            Integer completedLessonCount
    ) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (statDate == null) {
            throw new IllegalArgumentException("통계 날짜는 필수입니다.");
        }
        if (watchedLessonCount == null) {
            throw new IllegalArgumentException("수강량은 필수입니다.");
        }
        if (studySeconds == null) {
            throw new IllegalArgumentException("순공시간은 필수입니다.");
        }
        if (completedLessonCount == null) {
            throw new IllegalArgumentException("완료 영상 수는 필수입니다.");
        }
        if (watchedLessonCount < 0) {
            throw new IllegalArgumentException("수강량은 0 이상이어야 합니다.");
        }
        if (studySeconds < 0) {
            throw new IllegalArgumentException("순공시간은 0 이상이어야 합니다.");
        }
        if (completedLessonCount < 0) {
            throw new IllegalArgumentException("완료 영상 수는 0 이상이어야 합니다.");
        }
    }
}
