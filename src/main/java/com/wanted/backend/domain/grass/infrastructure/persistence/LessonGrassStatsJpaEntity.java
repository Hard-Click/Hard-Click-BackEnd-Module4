package com.wanted.backend.domain.grass.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "daily_study_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonGrassStatsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_study_stat_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "watched_lesson_count", nullable = false)
    private Integer watchedLessonCount;

    public LessonGrassStatsJpaEntity(
            Long memberId,
            LocalDate statDate,
            Integer watchedLessonCount
    ) {
        this.memberId = memberId;
        this.statDate = statDate;
        this.watchedLessonCount = watchedLessonCount;
    }
}
