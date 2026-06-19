package com.wanted.backend.domain.stats.application.usecase;

import com.wanted.backend.domain.stats.application.query.GetDailyStudyStatQuery;

import java.time.LocalDate;

public interface GetDailyStudyStatUseCase {

    DailyStudyStatView handle(GetDailyStudyStatQuery query);

    record DailyStudyStatView(
            LocalDate date,
            Integer watchedLessonCount,
            Integer studySeconds,
            Integer completedLessonCount
    ) {
    }
}
