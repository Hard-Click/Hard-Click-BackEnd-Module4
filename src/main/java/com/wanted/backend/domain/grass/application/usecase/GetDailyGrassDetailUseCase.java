package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetDailyGrassDetailQuery;

import java.time.LocalDate;

public interface GetDailyGrassDetailUseCase {

    DailyGrassDetailView handle(GetDailyGrassDetailQuery query);

    record DailyGrassDetailView(
            LocalDate date,
            Integer watchedLessonCount,
            Integer studySeconds,
            Boolean hasStudyRecord
    ) {
    }
}
