package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;
import com.wanted.backend.domain.grass.domain.policy.StudyTimeGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy;
import com.wanted.backend.domain.grass.domain.policy.YearlyGrassPeriodPolicy.YearlyGrassPeriod;
import com.wanted.backend.domain.grass.domain.repository.StudyTimeGrassRepository;
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
public class GetStudyTimeGrassService implements GetStudyTimeGrassUseCase {

    private final StudyTimeGrassRepository studyTimeGrassRepository;
    private final StudyTimeGrassLevelPolicy studyTimeGrassLevelPolicy;
    private final YearlyGrassPeriodPolicy yearlyGrassPeriodPolicy;
    private final Clock clock;

    @Override
    public List<StudyTimeGrassView> handle(GetStudyTimeGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        int year = query.year() != null ? query.year() : today.getYear();
        YearlyGrassPeriod period = yearlyGrassPeriodPolicy.calculate(year, today);

        List<StudyTimeGrassStat> stats = findStats(query, period);

        Map<LocalDate, Integer> studySecondsByDate = stats.stream()
                .collect(Collectors.toMap(
                        StudyTimeGrassStat::statDate,
                        StudyTimeGrassStat::studySeconds,
                        Integer::sum
                ));

        return period.startDate().datesUntil(period.endDate().plusDays(1))
                .map(date -> {
                    int studySeconds = studySecondsByDate.getOrDefault(date, 0);
                    return new StudyTimeGrassView(
                            date,
                            studySeconds,
                            studyTimeGrassLevelPolicy.calculate(studySeconds),
                            date.isAfter(today)
                    );
                })
                .toList();
    }

    private List<StudyTimeGrassStat> findStats(GetStudyTimeGrassQuery query, YearlyGrassPeriod period) {
        return period.queryableDateRange()
                .map(dateRange -> studyTimeGrassRepository.findByMemberIdAndDateBetween(
                        query.memberId(),
                        dateRange.startDate(),
                        dateRange.endDate()
                ))
                .orElseGet(List::of);
    }
}
