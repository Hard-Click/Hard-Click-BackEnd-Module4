package com.wanted.backend.domain.grass.application.mapper;

import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassDayView;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase.MonthlyGrassView;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy.MonthlyGrassPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MonthlyGrassViewMapper {

    private final LessonGrassLevelPolicy lessonGrassLevelPolicy;

    public MonthlyGrassView toView(
            Integer year,
            Integer month,
            MonthlyGrassPeriod period,
            Map<LocalDate, Integer> watchedLessonCountByDate,
            LocalDate today
    ) {
        List<MonthlyGrassDayView> days = period.startDate().datesUntil(period.endDate().plusDays(1))
                .map(date -> toDayView(date, watchedLessonCountByDate.getOrDefault(date, 0), date.isAfter(today)))
                .collect(Collectors.toCollection(ArrayList::new));

        return new MonthlyGrassView(year, month, days);
    }

    private MonthlyGrassDayView toDayView(LocalDate date, int value, boolean isFuture) {
        return new MonthlyGrassDayView(
                date,
                value,
                lessonGrassLevelPolicy.calculate(value),
                isFuture
        );
    }
}
