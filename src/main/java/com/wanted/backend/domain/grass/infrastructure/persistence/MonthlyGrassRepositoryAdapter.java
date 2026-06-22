package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.MonthlyGrassStat;
import com.wanted.backend.domain.grass.domain.repository.MonthlyGrassRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MonthlyGrassRepositoryAdapter implements MonthlyGrassRepository {

    private final SpringDataGrassDailyStudyStatsRepository repository;

    @Override
    public List<MonthlyGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
        return repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(memberId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MonthlyGrassStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new MonthlyGrassStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getWatchedLessonCount()
        );
    }
}
