package com.wanted.backend.domain.stats.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

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
        if (memberId == null
                || statDate == null
                || watchedLessonCount == null
                || studySeconds == null
                || completedLessonCount == null
                || watchedLessonCount < 0
                || studySeconds < 0
                || completedLessonCount < 0
        ) {
            throw new BusinessException(ErrorCode.DAILY_STATS_INVALID);
        }
    }
}
