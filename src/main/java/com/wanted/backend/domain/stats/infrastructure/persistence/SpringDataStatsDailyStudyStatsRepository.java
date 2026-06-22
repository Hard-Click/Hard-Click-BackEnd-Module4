package com.wanted.backend.domain.stats.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.infrastructure.persistence.DailyStudyStatsJpaEntity;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataStatsDailyStudyStatsRepository extends Repository<DailyStudyStatsJpaEntity, Long> {

    Optional<DailyStudyStatsJpaEntity> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);
}
