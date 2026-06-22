package com.wanted.backend.domain.grass.application.query;

import java.time.LocalDate;

public record GetDailyGrassDetailQuery(
        Long memberId,
        LocalDate date
) {
}
