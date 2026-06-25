package com.wanted.backend.domain.order.infrastructure.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderEnrollmentRefRepository extends JpaRepository<OrderEnrollmentRefEntity, Long> {

    @Query("SELECT e FROM OrderEnrollmentRef e WHERE e.memberId = :memberId AND e.courseId IN :courseIds")
    List<OrderEnrollmentRefEntity> findByMemberAndCourseIds(@Param("memberId") Long memberId,
                                                            @Param("courseIds") List<Long> courseIds);
}
