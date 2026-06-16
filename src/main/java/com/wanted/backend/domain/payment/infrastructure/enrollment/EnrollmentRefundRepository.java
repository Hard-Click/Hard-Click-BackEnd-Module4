package com.wanted.backend.domain.payment.infrastructure.enrollment;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface EnrollmentRefundRepository extends JpaRepository<EnrollmentRefundReferenceEntity, Long> {

    Optional<EnrollmentRefundReferenceEntity> findByMemberIdAndCourseId(Long memberId, Long courseId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE EnrollmentRefundReferenceEntity e SET e.status = :status WHERE e.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") EnrollmentStatus status);
}
