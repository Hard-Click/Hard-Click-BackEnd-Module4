package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.GrassViewMode;
import com.wanted.backend.domain.grass.domain.policy.GrassViewModePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetGrassViewService implements GetGrassViewUseCase {

    private final GetMonthlyGrassUseCase getMonthlyGrassUseCase;
    private final GetYearlyGrassUseCase getYearlyGrassUseCase;
    private final GrassViewModePolicy grassViewModePolicy;

    @Override
    public GrassView handle(GetGrassViewQuery query) {
        GrassViewMode viewMode = grassViewModePolicy.resolve(query.view());

        return switch (viewMode) {
            case MONTHLY -> monthly(query);
            case YEARLY -> yearly(query);
        };
    }

    private GrassView monthly(GetGrassViewQuery query) {
        GetMonthlyGrassUseCase.MonthlyGrassView view = getMonthlyGrassUseCase.handle(
                new GetMonthlyGrassQuery(
                        query.memberId(),
                        query.year(),
                        grassViewModePolicy.requireMonthForMonthly(query.month())
                )
        );

        return new GrassView(
                GrassViewMode.MONTHLY.value(),
                view.year(),
                view.month(),
                view.days().stream()
                        .map(day -> new GrassDayView(
                                day.date(),
                                day.value(),
                                day.level(),
                                day.isFuture()
                        ))
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private GrassView yearly(GetGrassViewQuery query) {
        GetYearlyGrassUseCase.YearlyGrassView view = getYearlyGrassUseCase.handle(
                new GetYearlyGrassQuery(
                        query.memberId(),
                        query.year()
                )
        );

        return new GrassView(
                GrassViewMode.YEARLY.value(),
                view.year(),
                null,
                view.days().stream()
                        .map(day -> new GrassDayView(
                                day.date(),
                                day.value(),
                                day.level(),
                                day.isFuture()
                        ))
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }
}
