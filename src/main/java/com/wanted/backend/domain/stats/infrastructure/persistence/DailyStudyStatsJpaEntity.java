package com.wanted.backend.domain.stats.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "daily_study_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_study_stats_member_id_stat_date",
                        columnNames = {"member_id", "stat_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStudyStatsJpaEntity {

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

    @Column(name = "study_seconds", nullable = false)
    private Integer studySeconds;

    @Column(name = "completed_lesson_count", nullable = false)
    private Integer completedLessonCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DailyStudyStatsJpaEntity(
            Long memberId,
            LocalDate statDate,
            Integer watchedLessonCount,
            Integer studySeconds,
            Integer completedLessonCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.memberId = memberId;
        this.statDate = statDate;
        this.watchedLessonCount = watchedLessonCount;
        this.studySeconds = studySeconds;
        this.completedLessonCount = completedLessonCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
