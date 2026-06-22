package com.wanted.backend.domain.grass.application.usecase;

import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;

import java.time.LocalDate;
import java.util.List;

public interface GetGrassViewUseCase {

    GrassView handle(GetGrassViewQuery query);

    record GrassView(
            String view,
            Integer year,
            Integer month,
            List<GrassDayView> days
    ) {
    }

    record GrassDayView(
            LocalDate date,
            Integer value,
            Integer level,
            Boolean isFuture
    ) {
    }
}
