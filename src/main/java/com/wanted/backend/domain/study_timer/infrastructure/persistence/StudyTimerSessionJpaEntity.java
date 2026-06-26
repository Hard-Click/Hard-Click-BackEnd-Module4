package com.wanted.backend.domain.study_timer.infrastructure.persistence;

import com.wanted.backend.domain.study_timer.domain.model.StudyTimerSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "study_timer_sessions",
        indexes = {
                @Index(name = "idx_study_timer_sessions_member_id_status", columnList = "member_id,status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyTimerSessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_timer_session_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "accumulated_study_seconds", nullable = false)
    private Integer accumulatedStudySeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudyTimerSessionStatus status;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudyTimerSessionJpaEntity(
            Long memberId,
            Long courseId,
            Long lessonId,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.memberId = memberId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.accumulatedStudySeconds = accumulatedStudySeconds;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateSession(
            Long courseId,
            Long lessonId,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            Integer accumulatedStudySeconds,
            StudyTimerSessionStatus status,
            LocalDateTime pausedAt,
            LocalDateTime updatedAt
    ) {
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.accumulatedStudySeconds = accumulatedStudySeconds;
        this.status = status;
        this.pausedAt = pausedAt;
        this.updatedAt = updatedAt;
    }
}
