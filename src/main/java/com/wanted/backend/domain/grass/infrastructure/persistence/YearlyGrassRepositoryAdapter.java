package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.YearlyGrassStat;
import com.wanted.backend.domain.grass.domain.repository.YearlyGrassRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class YearlyGrassRepositoryAdapter implements YearlyGrassRepository {

    private final SpringDataGrassDailyStudyStatsRepository repository;

    @Override
    public List<YearlyGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
        return repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(memberId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private YearlyGrassStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new YearlyGrassStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getWatchedLessonCount()
        );
    }
}
