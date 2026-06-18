package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import com.wanted.backend.domain.study_timer.domain.repository.DailyStudyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStudyStatsRepositoryAdapter implements DailyStudyStatsRepository {

    private final SpringDataDailyStudyStatsRepository repository;

    @Override
    public List<DailyStudyStat> findByMemberIdAndDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return repository.findByMemberIdAndStudyDateBetweenOrderByStudyDateAsc(memberId, startDate, endDate).stream()
                .map(this::toDomain)
                .toList();
    }

    private DailyStudyStat toDomain(DailyStudyStatsJpaEntity entity) {
        return new DailyStudyStat(
                entity.getMemberId(),
                entity.getStudyDate(),
                entity.getStudySeconds()
        );
    }
}
