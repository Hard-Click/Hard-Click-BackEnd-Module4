package com.wanted.backend.domain.grass.domain.model;

import java.time.LocalDate;

public record StudyStreakStat(
        LocalDate statDate,
        Integer watchedLessonCount,
        Integer studySeconds
) {
}
