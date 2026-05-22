package com.wanted.backend.domain.learning_activity.infrastructure.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataEnrollmentAccessRepository extends JpaRepository<EnrollmentReferenceJpaEntity, Long> {

    @Query("""
            SELECT COUNT(e) > 0
            FROM EnrollmentReferenceJpaEntity e
            WHERE e.memberId = :memberId
              AND e.courseId = :courseId
              AND e.status IN ('ENROLLED', 'COMPLETED')
              AND (e.expiredAt IS NULL OR e.expiredAt >= CURRENT_TIMESTAMP)
            """)
    boolean existsActiveEnrollment(@Param("memberId") Long memberId, @Param("courseId") Long courseId);
}
