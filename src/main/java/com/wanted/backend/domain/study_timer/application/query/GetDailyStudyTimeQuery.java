package com.wanted.backend.domain.study_timer.application.query;

import java.time.LocalDate;

public record GetDailyStudyTimeQuery(
        Long memberId,
        LocalDate startDate,
        LocalDate endDate
) {
}
