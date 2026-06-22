package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.mapper.YearlyGrassViewMapper;
import com.wanted.backend.domain.grass.application.query.GetYearlyGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy.YearlyGrassPeriod;
import com.wanted.backend.domain.grass.domain.repository.YearlyGrassRepository;
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
public class GetYearlyGrassService implements GetYearlyGrassUseCase {

    private final YearlyGrassRepository yearlyGrassRepository;
    private final YearlyGrassViewMapper yearlyGrassViewMapper;
    private final YearlyGrassPeriodPolicy yearlyGrassPeriodPolicy;
    private final Clock clock;

    @Override
    public YearlyGrassView handle(GetYearlyGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        YearlyGrassPeriod period = yearlyGrassPeriodPolicy.calculate(query.year(), today);

        List<YearlyGrassStat> stats = findStats(query, period);

        Map<LocalDate, YearlyGrassStat> statByDate = stats.stream()
                .collect(Collectors.toMap(
                        YearlyGrassStat::statDate,
                        stat -> stat,
                        YearlyGrassStat::merge
                ));

        return yearlyGrassViewMapper.toView(query.year(), period, statByDate, today);
    }

    private List<YearlyGrassStat> findStats(GetYearlyGrassQuery query, YearlyGrassPeriod period) {
        return period.queryableDateRange()
                .map(dateRange -> yearlyGrassRepository.findByMemberIdAndDateBetween(
                        query.memberId(),
                        dateRange.startDate(),
                        dateRange.endDate()
                ))
                .orElseGet(List::of);
    }

}
