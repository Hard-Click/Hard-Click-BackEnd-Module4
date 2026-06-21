package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.DailyGrassDetailStat;
import com.wanted.backend.domain.grass.domain.repository.DailyGrassDetailRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyGrassDetailRepositoryAdapter implements DailyGrassDetailRepository {

    private final SpringDataGrassDailyStudyStatsRepository repository;

    @Override
    public Optional<DailyGrassDetailStat> findByMemberIdAndStatDate(Long memberId, LocalDate statDate) {
        return repository.findByMemberIdAndStatDate(memberId, statDate)
                .map(this::toDomain);
    }

    private DailyGrassDetailStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new DailyGrassDetailStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getWatchedLessonCount(),
                entity.getStudySeconds()
        );
    }
}
