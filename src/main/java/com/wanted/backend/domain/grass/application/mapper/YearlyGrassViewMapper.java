package com.wanted.backend.domain.grass.application.mapper;

import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase.YearlyGrassView;
import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy.YearlyGrassPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class YearlyGrassViewMapper {

    private final LessonGrassLevelPolicy lessonGrassLevelPolicy;

    public YearlyGrassView toView(
            Integer year,
            YearlyGrassPeriod period,
            Map<LocalDate, YearlyGrassStat> statByDate,
            LocalDate today
    ) {
        List<YearlyGrassDayView> days = period.startDate().datesUntil(period.endDate().plusDays(1))
                .map(date -> toDayView(date, statByDate.get(date), date.isAfter(today)))
                .toList();

        return new YearlyGrassView(year, days);
    }

    private YearlyGrassDayView toDayView(LocalDate date, YearlyGrassStat stat, boolean isFuture) {
        int value = stat == null ? 0 : stat.watchedLessonCount();

        return new YearlyGrassDayView(
                date,
                value,
                lessonGrassLevelPolicy.calculate(value),
                isFuture
        );
    }
}
