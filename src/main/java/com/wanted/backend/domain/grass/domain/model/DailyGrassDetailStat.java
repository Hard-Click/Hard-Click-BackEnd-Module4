package com.wanted.backend.domain.grass.domain.model;

import java.time.LocalDate;

public record DailyGrassDetailStat(
        Long memberId,
        LocalDate statDate,
        Integer watchedLessonCount,
        Integer studySeconds
) {

    public static DailyGrassDetailStat empty(Long memberId, LocalDate statDate) {
        return new DailyGrassDetailStat(memberId, statDate, 0, 0);
    }
}
