package com.wanted.backend.domain.grass.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataLessonGrassStatsRepository extends JpaRepository<LessonGrassStatsJpaEntity, Long> {

    List<LessonGrassStatsJpaEntity> findByMemberIdAndStatDateBetweenOrderByStatDateAsc(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
}
