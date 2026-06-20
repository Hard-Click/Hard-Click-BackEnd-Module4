package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import com.wanted.backend.domain.grass.domain.policy.StudyStreakPolicy;
import com.wanted.backend.domain.grass.domain.repository.StudyStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyStreakService implements GetStudyStreakUseCase {

    private final StudyStreakRepository studyStreakRepository;
    private final StudyStreakPolicy studyStreakPolicy;
    private final Clock clock;

    @Override
    public StudyStreakView handle(GetStudyStreakQuery query) {
        LocalDate today = LocalDate.now(clock);
        List<StudyStreakStat> stats = studyStreakRepository.findByMemberIdAndDateLessThanEqual(
                query.memberId(),
                today
        );

        return new StudyStreakView(
                studyStreakPolicy.calculate(today, stats)
        );
    }
}
