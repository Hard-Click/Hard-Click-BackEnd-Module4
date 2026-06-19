package com.wanted.backend.domain.grass.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataGrassDailyStudyStatsRepository extends Repository<DailyStudyStatsJpaEntity, Long> {

    List<DailyStudyStatsJpaEntity> findByMemberIdAndStatDateBetweenOrderByStatDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
