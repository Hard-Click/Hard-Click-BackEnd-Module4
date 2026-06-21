package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataDailyStudyStatsRepository extends JpaRepository<DailyStudyStatsJpaEntity, Long> {

    List<DailyStudyStatsJpaEntity> findByMemberIdAndStatDateBetweenOrderByStatDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
