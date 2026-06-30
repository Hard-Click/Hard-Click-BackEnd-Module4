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
    @Cacheable(cacheNames = "grassLessons:v3", key = "#root.target.resolveCacheKey(#query)")
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

    /**
     * 과거 연도는 오늘 날짜가 바뀌어도 결과가 달라지지 않으므로 연도만으로 키를 고정하고,
     * 현재(또는 미래) 연도만 오늘 날짜를 키에 포함해 day 단위로 갱신한다.
     */
    public String resolveCacheKey(GetLessonGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        int year = query.year() != null ? query.year() : today.getYear();
        String key = query.memberId() + ":" + year;
        return year >= today.getYear() ? key + ":" + today : key;
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
