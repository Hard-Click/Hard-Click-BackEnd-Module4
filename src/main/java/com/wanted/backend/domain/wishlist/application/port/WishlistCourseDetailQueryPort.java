package com.wanted.backend.domain.wishlist.application.port;

import java.util.List;

public interface WishlistCourseDetailQueryPort {

    List<CourseDetail> findAllByIds(List<Long> courseIds, Long memberId);

    record CourseDetail(
            Long courseId,
            String title,
            String subject,
            String thumbnailUrl,
            String priceType,
            String instructorName,
            Integer price,
            Double averageRating,
            Integer reviewCount,
            Integer enrollmentCount,
            boolean enrolled,
            boolean inCart
    ) {}
}
