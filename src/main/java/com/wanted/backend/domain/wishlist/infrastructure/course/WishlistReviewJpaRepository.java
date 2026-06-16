package com.wanted.backend.domain.wishlist.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistReviewJpaRepository extends JpaRepository<WishlistReviewJpaEntity, Long> {

    @Query("SELECT r.courseId, AVG(r.rating), COUNT(r.id) FROM WishlistReview r WHERE r.courseId IN :courseIds GROUP BY r.courseId")
    List<Object[]> findRatingStatsByCourseIds(@Param("courseIds") List<Long> courseIds);
}
