package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy.YearlyGrassPeriod;
import com.wanted.backend.domain.grass.domain.repository.LessonGrassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLessonGrassService implements GetLessonGrassUseCase {

    private final LessonGrassRepository lessonGrassRepository;
    private final LessonGrassLevelPolicy lessonGrassLevelPolicy;
    private final YearlyGrassPeriodPolicy yearlyGrassPeriodPolicy;
    private final Clock clock;

    @Override
    @Cacheable(cacheNames = "grassLessons:v2", key = "#query.memberId() + ':' + (#query.year() != null ? #query.year() : T(java.time.LocalDate).now(@clock).getYear()) + ':' + T(java.time.LocalDate).now(@clock)")
    public List<LessonGrassView> handle(GetLessonGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        int year = query.year() != null ? query.year() : today.getYear();
        YearlyGrassPeriod period = yearlyGrassPeriodPolicy.calculate(year, today);

        List<LessonGrassStat> stats = findStats(query, period);

        Map<LocalDate, Integer> watchedLessonCountByDate = stats.stream()
                .collect(Collectors.toMap(
                        LessonGrassStat::statDate,
                        LessonGrassStat::watchedLessonCount,
                        Integer::sum
                ));

        return period.startDate().datesUntil(period.endDate().plusDays(1))
                .map(date -> {
                    int watchedLessonCount = watchedLessonCountByDate.getOrDefault(date, 0);
                    return new LessonGrassView(
                            date,
                            watchedLessonCount,
                            lessonGrassLevelPolicy.calculate(watchedLessonCount),
                            date.isAfter(today)
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<LessonGrassStat> findStats(GetLessonGrassQuery query, YearlyGrassPeriod period) {
        return period.queryableDateRange()
                .map(dateRange -> lessonGrassRepository.findByMemberIdAndDateBetween(
                        query.memberId(),
                        dateRange.startDate(),
                        dateRange.endDate()
                ))
                .orElseGet(List::of);
    }
}
