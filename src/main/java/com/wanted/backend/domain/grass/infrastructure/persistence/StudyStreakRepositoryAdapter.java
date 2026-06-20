package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import com.wanted.backend.domain.grass.domain.repository.StudyStreakRepository;
import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudyStreakRepositoryAdapter implements StudyStreakRepository {

    private final SpringDataGrassDailyStudyStatsRepository repository;

    @Override
    public List<StudyStreakStat> findByMemberIdAndDateLessThanEqual(
            Long memberId,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    ) {
        return repository.findByMemberIdAndStatDateLessThanEqualOrderByStatDateDesc(
                        memberId,
                        endDate,
                        PageRequest.of(pageNumber, pageSize)
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private StudyStreakStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new StudyStreakStat(
                entity.getStatDate(),
                entity.getWatchedLessonCount(),
                entity.getStudySeconds()
        );
    }
}
