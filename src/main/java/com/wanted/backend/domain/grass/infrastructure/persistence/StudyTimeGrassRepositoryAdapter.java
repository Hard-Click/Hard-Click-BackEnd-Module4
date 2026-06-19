package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.StudyTimeGrassStat;
import com.wanted.backend.domain.grass.domain.repository.StudyTimeGrassRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudyTimeGrassRepositoryAdapter implements StudyTimeGrassRepository {

    private final SpringDataGrassDailyStudyStatsRepository repository;

    @Override
    public List<StudyTimeGrassStat> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
        return repository.findByMemberIdAndStatDateBetweenOrderByStatDateAsc(memberId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private StudyTimeGrassStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new StudyTimeGrassStat(
                entity.getMemberId(),
                entity.getStatDate(),
                entity.getStudySeconds()
        );
    }
}
