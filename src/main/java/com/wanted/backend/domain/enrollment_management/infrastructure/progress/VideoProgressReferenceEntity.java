package com.wanted.backend.domain.enrollment_management.infrastructure.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Getter
@Immutable
@Table(name = "video_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoProgressReferenceEntity {

    @Id
    @Column(name = "progress_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "last_position_sec", nullable = false)
    private Integer lastPositionSeconds;

    @Column(name = "is_completed", nullable = false)
    private Boolean completed;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
