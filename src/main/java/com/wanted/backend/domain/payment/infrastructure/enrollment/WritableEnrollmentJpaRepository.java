package com.wanted.backend.domain.payment.infrastructure.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WritableEnrollmentJpaRepository extends JpaRepository<WritableEnrollmentJpaEntity, Long> {

    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);
}
