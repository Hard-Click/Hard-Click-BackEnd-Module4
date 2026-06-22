package com.wanted.backend.domain.grass.domain.model;

import java.time.LocalDate;

public record StudyTimeGrassStat(
        Long memberId,
        LocalDate statDate,
        Integer studySeconds
) {
}
