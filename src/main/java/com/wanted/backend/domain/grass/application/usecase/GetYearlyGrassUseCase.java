package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetYearlyGrassUseCase {

    YearlyGrassView handle(GetYearlyGrassQuery query);

    record YearlyGrassView(
            Integer year,
            List<YearlyGrassDayView> days
    ) {
    }

    record YearlyGrassDayView(
            LocalDate date,
            Integer value,
            Integer level,
            Boolean isFuture
    ) {
    }
}
