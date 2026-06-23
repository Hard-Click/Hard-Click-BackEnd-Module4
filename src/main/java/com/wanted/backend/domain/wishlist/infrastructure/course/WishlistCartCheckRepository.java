package com.wanted.backend.domain.wishlist.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistCartCheckRepository extends JpaRepository<WishlistCartCheckEntity, Long> {

    @Query("SELECT c.courseId FROM WishlistCartCheck c WHERE c.memberId = :memberId AND c.courseId IN :courseIds")
    List<Long> findInCartCourseIds(@Param("memberId") Long memberId, @Param("courseIds") List<Long> courseIds);
}
