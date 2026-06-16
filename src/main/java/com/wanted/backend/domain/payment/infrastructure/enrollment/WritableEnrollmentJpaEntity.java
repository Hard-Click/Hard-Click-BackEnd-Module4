package com.wanted.backend.domain.payment.infrastructure.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "WritableEnrollment")
@Getter
@Table(name = "enrollment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WritableEnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "enrolled_at", nullable = false)
    private java.time.Instant enrolledAt;

    @Column(nullable = false, length = 20)
    private String status;

    public static WritableEnrollmentJpaEntity create(Long memberId, Long courseId) {
        WritableEnrollmentJpaEntity e = new WritableEnrollmentJpaEntity();
        e.memberId = memberId;
        e.courseId = courseId;
        e.enrolledAt = java.time.Instant.now();
        e.status = "ENROLLED";
        return e;
    }
}
