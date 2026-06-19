package com.wanted.backend.domain.grass.application.mapper;

import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassView;
import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy.YearlyGrassPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class YearlyGrassViewMapperTest {

    @Test
    void mapsYearlyGrassStatsToView() {
        YearlyGrassViewMapper mapper = new YearlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        YearlyGrassPeriod period = new YearlyGrassPeriod(
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-03"),
                LocalDate.parse("2026-01-02")
        );
        Map<LocalDate, YearlyGrassStat> statByDate = Map.of(
                LocalDate.parse("2026-01-01"),
                new YearlyGrassStat(1L, LocalDate.parse("2026-01-01"), 2)
        );

        YearlyGrassView result = mapper.toView(
                2026,
                period,
                statByDate,
                LocalDate.parse("2026-01-02")
        );

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.days())
                .extracting(
                        YearlyGrassDayView::date,
                        YearlyGrassDayView::value,
                        YearlyGrassDayView::level,
                        YearlyGrassDayView::isFuture
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-01-01"), 2, 2, false),
                        tuple(LocalDate.parse("2026-01-02"), 0, 0, false),
                        tuple(LocalDate.parse("2026-01-03"), 0, 0, true)
                );
    }
}
