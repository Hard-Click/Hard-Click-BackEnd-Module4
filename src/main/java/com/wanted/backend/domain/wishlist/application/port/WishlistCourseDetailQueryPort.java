package com.wanted.backend.domain.wishlist.application.port;

import java.util.List;

public interface WishlistCourseDetailQueryPort {

    List<CourseDetail> findAllByIds(List<Long> courseIds, Long memberId);

    record CourseDetail(
            Long courseId,
            String title,
            String instructorName,
            Integer price,
            Double averageRating,
            Integer reviewCount,
            boolean enrolled,
            boolean inCart
    ) {}
}
