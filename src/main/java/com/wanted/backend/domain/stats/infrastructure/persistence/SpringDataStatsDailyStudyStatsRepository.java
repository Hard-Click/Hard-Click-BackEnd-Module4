package com.wanted.backend.domain.stats.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataStatsDailyStudyStatsRepository extends JpaRepository<StatsDailyStudyStatsJpaEntity, Long> {

    Optional<StatsDailyStudyStatsJpaEntity> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);
}
