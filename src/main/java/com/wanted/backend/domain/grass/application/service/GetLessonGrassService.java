package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetLessonGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import com.wanted.backend.domain.grass.domain.policy.LessonGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.repository.LessonGrassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
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
    private final Clock clock;

    @Override
    @Cacheable(cacheNames = "grassLessons", key = "#query.memberId() + ':' + T(java.time.LocalDate).now(@clock)")
    public List<LessonGrassView> handle(GetLessonGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = LocalDate.of(today.getYear(), Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(today.getYear(), Month.DECEMBER, 31);

        List<LessonGrassStat> stats = lessonGrassRepository.findByMemberIdAndDateBetween(
                query.memberId(),
                startDate,
                today
        );

        Map<LocalDate, Integer> watchedLessonCountByDate = stats.stream()
                .collect(Collectors.toMap(
                        LessonGrassStat::statDate,
                        LessonGrassStat::watchedLessonCount,
                        Integer::sum
                ));

        return startDate.datesUntil(endDate.plusDays(1))
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
}
