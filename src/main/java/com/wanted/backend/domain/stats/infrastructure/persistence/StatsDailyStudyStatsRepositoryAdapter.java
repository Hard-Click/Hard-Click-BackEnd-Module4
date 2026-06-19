package com.wanted.backend.domain.stats.infrastructure.persistence;

import com.wanted.backend.domain.stats.domain.model.DailyStudyStat;
import com.wanted.backend.domain.stats.domain.repository.DailyStudyStatsRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StatsDailyStudyStatsRepositoryAdapter implements DailyStudyStatsRepository {

    private final SpringDataStatsDailyStudyStatsRepository repository;

    @Override
    public Optional<DailyStudyStat> findByMemberIdAndStatDate(Long memberId, LocalDate statDate) {
        return repository.findByMemberIdAndStatDate(memberId, statDate)
                .map(this::toDomain);
    }

    private DailyStudyStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new DailyStudyStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getWatchedLessonCount(),
                entity.getStudySeconds(),
                entity.getCompletedLessonCount()
        );
    }
}
