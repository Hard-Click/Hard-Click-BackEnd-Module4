package com.wanted.backend.domain.cource.application.dto;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;

import java.time.Instant;
import java.util.List;

public record CourseListResult(
        List<Item> items,
        int currentPage,
        int totalPages,
        long totalCount
) {
    public record Item(
            Long courseId,
            String title,
            String subject,
            String thumbnailUrl,
            PriceType priceType,
            int price,
            String instructorName,
            double rating,
            int reviewCount,
            int studentCount,
            CourseStatus status,
            Instant createdAt
    ) {}
}
