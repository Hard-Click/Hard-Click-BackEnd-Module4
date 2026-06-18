package com.wanted.backend.domain.stats.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataDailyStudyStatsRepository extends JpaRepository<DailyStudyStatsJpaEntity, Long> {

    Optional<DailyStudyStatsJpaEntity> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);
}
