package com.wanted.backend.domain.learning_activity.infrastructure.enrollment;

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
@Table(name = "enrollments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentReferenceJpaEntity {

    @Id
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}
