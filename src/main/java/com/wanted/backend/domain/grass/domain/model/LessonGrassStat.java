package com.wanted.backend.domain.grass.domain.model;

import java.time.LocalDate;

public record LessonGrassStat(
        Long memberId,
        LocalDate statDate,
        Integer watchedLessonCount
) {
}
