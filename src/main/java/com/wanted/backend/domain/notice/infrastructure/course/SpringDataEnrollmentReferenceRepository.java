package com.wanted.backend.domain.notice.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataEnrollmentReferenceRepository extends JpaRepository<EnrollmentReferenceEntity, Long> {
    @Query("SELECT e.courseId FROM NoticeEnrollmentReference e WHERE e.memberId = :memberId")
    List<Long> findCourseIdsByMemberId(@Param("memberId") Long memberId);
}