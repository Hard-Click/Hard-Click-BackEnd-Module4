package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import com.wanted.backend.domain.grass.domain.policy.StudyStreakPolicy;
import com.wanted.backend.domain.grass.domain.repository.StudyStreakRepository;
import com.wanted.backend.domain.grass.infrastructure.config.GrassStreakProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyStreakService implements GetStudyStreakUseCase {

    private final StudyStreakRepository studyStreakRepository;
    private final StudyStreakPolicy studyStreakPolicy;
    private final Clock clock;
    private final GrassStreakProperties grassStreakProperties;

    @Override
    public StudyStreakView handle(GetStudyStreakQuery query) {
        LocalDate today = LocalDate.now(clock);
        int pageSize = grassStreakProperties.queryPageSize();
        List<StudyStreakStat> stats = new ArrayList<>();
        int pageNumber = 0;
        int streak = 0;

        while (true) {
            List<StudyStreakStat> page = studyStreakRepository.findByMemberIdAndDateLessThanEqual(
                    query.memberId(),
                    today,
                    pageNumber,
                    pageSize
            );

            if (page.isEmpty()) {
                break;
            }

            stats.addAll(page);
            streak = studyStreakPolicy.calculate(today, stats);
            if (page.size() < pageSize || streak < stats.size()) {
                break;
            }

            pageNumber++;
        }

        return new StudyStreakView(streak);
    }
}
