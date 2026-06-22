package com.wanted.backend.domain.grass.application.mapper;

import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassView;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy.MonthlyGrassPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class MonthlyGrassViewMapperTest {

    @Test
    void mapsMonthlyGrassStatsToView() {
        MonthlyGrassViewMapper mapper = new MonthlyGrassViewMapper(new LessonGrassLevelPolicy(4));
        MonthlyGrassPeriod period = new MonthlyGrassPeriod(
                LocalDate.parse("2026-06-01"),
                LocalDate.parse("2026-06-03"),
                LocalDate.parse("2026-06-02")
        );
        Map<LocalDate, Integer> watchedLessonCountByDate = Map.of(
                LocalDate.parse("2026-06-01"),
                2
        );

        MonthlyGrassView result = mapper.toView(
                2026,
                6,
                period,
                watchedLessonCountByDate,
                LocalDate.parse("2026-06-02")
        );

        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(6);
        assertThat(result.days())
                .extracting(
                        MonthlyGrassDayView::date,
                        MonthlyGrassDayView::value,
                        MonthlyGrassDayView::level,
                        MonthlyGrassDayView::isFuture
                )
                .containsExactly(
                        tuple(LocalDate.parse("2026-06-01"), 2, 2, false),
                        tuple(LocalDate.parse("2026-06-02"), 0, 0, false),
                        tuple(LocalDate.parse("2026-06-03"), 0, 0, true)
                );
    }
}
