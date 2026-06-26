package com.wanted.backend.domain.wishlist.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistEnrollmentCheckRepository extends JpaRepository<WishlistEnrollmentCheckEntity, Long> {

    @Query("SELECT e.courseId FROM WishlistEnrollment e WHERE e.memberId = :memberId AND e.courseId IN :courseIds")
    List<Long> findEnrolledCourseIds(@Param("memberId") Long memberId, @Param("courseIds") List<Long> courseIds);

    @Query("SELECT e.courseId, COUNT(e.id) FROM WishlistEnrollment e WHERE e.courseId IN :courseIds GROUP BY e.courseId")
    List<Object[]> countEnrollmentsByCourseIds(@Param("courseIds") List<Long> courseIds);
}
