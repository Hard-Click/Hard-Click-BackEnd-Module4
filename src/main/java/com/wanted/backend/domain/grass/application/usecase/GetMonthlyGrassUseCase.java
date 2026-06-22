package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetMonthlyGrassUseCase {

    MonthlyGrassView handle(GetMonthlyGrassQuery query);

    record MonthlyGrassView(
            Integer year,
            Integer month,
            List<MonthlyGrassDayView> days
    ) {
    }

    record MonthlyGrassDayView(
            LocalDate date,
            Integer value,
            Integer level,
            Boolean isFuture
    ) {
    }
}
