package com.wanted.backend.domain.stats.application.query;

import java.time.LocalDate;

public record GetDailyStudyStatQuery(
        Long memberId,
        LocalDate date
) {
}
