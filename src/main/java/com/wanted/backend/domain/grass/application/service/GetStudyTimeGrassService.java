package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyTimeGrassQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;
import com.wanted.backend.domain.grass.domain.policy.StudyTimeGrassLevelPolicy;
import com.wanted.backend.domain.grass.domain.repository.StudyTimeGrassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyTimeGrassService implements GetStudyTimeGrassUseCase {

    private final StudyTimeGrassRepository studyTimeGrassRepository;
    private final StudyTimeGrassLevelPolicy studyTimeGrassLevelPolicy;
    private final Clock clock;

    @Override
    public List<StudyTimeGrassView> handle(GetStudyTimeGrassQuery query) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = LocalDate.of(today.getYear(), Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(today.getYear(), Month.DECEMBER, 31);

        List<StudyTimeGrassStat> stats = studyTimeGrassRepository.findByMemberIdAndDateBetween(
                query.memberId(),
                startDate,
                today
        );

        Map<LocalDate, Integer> studySecondsByDate = stats.stream()
                .collect(Collectors.toMap(
                        StudyTimeGrassStat::statDate,
                        StudyTimeGrassStat::studySeconds,
                        Integer::sum
                ));

        return startDate.datesUntil(endDate.plusDays(1))
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
}
