package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.mapper.MonthlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.query.GetMonthlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.MonthlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.MonthlyGrassPeriodPolicy.MonthlyGrassPeriod;
import com.wanted.backend.domain.grass.domain.repository.MonthlyGrassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMonthlyGrassService implements GetMonthlyGrassUseCase {

    private final MonthlyGrassRepository monthlyGrassRepository;
    private final MonthlyGrassViewMapper monthlyGrassViewMapper;
    private final MonthlyGrassPeriodPolicy monthlyGrassPeriodPolicy;
    private final Clock clock;

    @Override
    public MonthlyGrassView handle(GetMonthlyGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        MonthlyGrassPeriod period = monthlyGrassPeriodPolicy.calculate(query.year(), query.month(), today);

        List<MonthlyGrassStat> stats = findStats(query, period);

        Map<LocalDate, Integer> watchedLessonCountByDate = stats.stream()
                .collect(Collectors.toMap(
                        MonthlyGrassStat::statDate,
                        MonthlyGrassStat::watchedLessonCount,
                        Integer::sum
                ));

        return monthlyGrassViewMapper.toView(query.year(), query.month(), period, watchedLessonCountByDate, today);
    }

    private List<MonthlyGrassStat> findStats(GetMonthlyGrassQuery query, MonthlyGrassPeriod period) {
        return period.queryableDateRange()
                .map(dateRange -> monthlyGrassRepository.findByMemberIdAndDateBetween(
                        query.memberId(),
                        dateRange.startDate(),
                        dateRange.endDate()
                ))
                .orElseGet(List::of);
    }
}
