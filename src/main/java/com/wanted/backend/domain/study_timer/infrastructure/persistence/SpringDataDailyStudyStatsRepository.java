package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
            SELECT s.memberId AS memberId, SUM(s.studySeconds) AS totalSeconds
            FROM DailyStudyStatsJpaEntity s
            WHERE s.statDate BETWEEN :startDate AND :endDate
            GROUP BY s.memberId
            """)
    List<MemberStudySecondsSum> sumStudySecondsByDateBetween(LocalDate startDate, LocalDate endDate);
}
