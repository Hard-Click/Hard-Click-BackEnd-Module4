package com.wanted.backend.domain.payment.infrastructure.progress;

import org.springframework.data.jpa.repository.JpaRepository;

interface VideoProgressForRefundRepository extends JpaRepository<VideoProgressForRefundEntity, Long> {

    long countByMemberIdAndCourseIdAndCompleted(Long memberId, Long courseId, Boolean completed);
}
