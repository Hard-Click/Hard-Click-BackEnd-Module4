package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetStudyTimeGrassUseCase {

    List<StudyTimeGrassView> handle(GetStudyTimeGrassQuery query);

    record StudyTimeGrassView(
            LocalDate date,
            Integer studySeconds,
            Integer level,
            Boolean isFuture
    ) {
    }
}
