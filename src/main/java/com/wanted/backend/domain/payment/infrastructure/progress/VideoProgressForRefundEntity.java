package com.wanted.backend.domain.payment.infrastructure.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "video_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoProgressForRefundEntity {

    @Id
    @Column(name = "progress_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "is_completed", nullable = false)
    private Boolean completed;
}
