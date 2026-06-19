package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "daily_study_stats",
        indexes = {
                @Index(name = "idx_daily_study_stats_member_id_stat_date", columnList = "member_id,stat_date")
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

    @Column(name = "study_seconds", nullable = false)
    private Integer studySeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DailyStudyStatsJpaEntity(
            Long memberId,
            LocalDate statDate,
            Integer studySeconds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.memberId = memberId;
        this.statDate = statDate;
        this.studySeconds = studySeconds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
