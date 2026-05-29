package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;

import java.time.Instant;

public record CourseListItemResponse(
        Long courseId,
        String title,
        String subjectName,
        String thumbnailUrl,
        String priceLabel,   // "무료" or "99,000원"
        PriceType priceType,
        int price,
        String instructorName,
        double averageRating,
        int reviewCount,
        int studentCount,
        Instant createdAt,
        CourseStatus status
) {
    public static CourseListItemResponse from(CourseListResult.Item item) {
        String priceLabel = item.priceType() == PriceType.FREE
                ? "무료"
                : String.format("%,d원", item.price());
        return new CourseListItemResponse(
                item.courseId(), item.title(), item.subject(), item.thumbnailUrl(),
                priceLabel, item.priceType(), item.price(),
                item.instructorName(), item.rating(), item.reviewCount(), item.studentCount(),
                item.createdAt(), item.status()
        );
    }
}
