package com.wanted.backend.domain.ranking.infrastructure.persistence;

import com.wanted.backend.domain.ranking.application.port.StudyTimeScoreReader;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.MemberStudySecondsSum;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.SpringDataDailyStudyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyTimeScoreReaderAdapter implements StudyTimeScoreReader {

    private final SpringDataDailyStudyStatsRepository repository;

    @Override
    public Map<Long, Long> sumStudySecondsByDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.sumStudySecondsByDateBetween(startDate, endDate).stream()
                .collect(Collectors.toMap(
                        MemberStudySecondsSum::getMemberId,
                        MemberStudySecondsSum::getTotalSeconds
                ));
    }
}
