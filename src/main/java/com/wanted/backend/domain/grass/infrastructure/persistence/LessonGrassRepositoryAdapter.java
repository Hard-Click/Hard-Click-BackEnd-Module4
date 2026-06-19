package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.LessonGrassStat;
import com.wanted.backend.domain.grass.domain.repository.LessonGrassRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LessonGrassRepositoryAdapter implements LessonGrassRepository {

    private final SpringDataLessonGrassStatsRepository repository;

    @Override
    public List<LessonGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
        return repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(memberId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private LessonGrassStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new LessonGrassStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getWatchedLessonCount()
        );
    }
}
