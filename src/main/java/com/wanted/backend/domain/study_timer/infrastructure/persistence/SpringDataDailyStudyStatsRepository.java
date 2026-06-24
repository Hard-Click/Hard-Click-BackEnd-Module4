package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataDailyStudyStatsRepository extends JpaRepository<DailyStudyStatsJpaEntity, Long> {

    Optional<DailyStudyStatsJpaEntity> findByMemberIdAndStatDate(Long memberId, LocalDate statDate);

    List<DailyStudyStatsJpaEntity> findByMemberIdAndStatDateBetweenOrderByStatDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<DailyStudyStatsJpaEntity> findByStatDateBetween(LocalDate startDate, LocalDate endDate);
}
