package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetLessonGrassUseCase {

    List<LessonGrassView> handle(GetLessonGrassQuery query);

    record LessonGrassView(
            LocalDate date,
            Integer watchedLessonCount,
            Integer level,
            Boolean isFuture
    ) {
    }
}
