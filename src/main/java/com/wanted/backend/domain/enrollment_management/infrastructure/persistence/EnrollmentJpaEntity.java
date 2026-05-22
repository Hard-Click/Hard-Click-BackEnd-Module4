package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    static EnrollmentJpaEntity from(Enrollment enrollment) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity();
        entity.userId = enrollment.getUserId();
        entity.courseId = enrollment.getCourseId();
        entity.enrolledAt = enrollment.getEnrolledAt();
        return entity;
    }

    Enrollment toDomain() {
        return Enrollment.restore(id, userId, courseId, enrolledAt);
    }
}
