package com.wanted.backend.domain.cource.application.dto;

import com.wanted.backend.domain.cource.domain.model.PriceType;

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
            double rating,       // 추후 리뷰 도메인 연동
            int reviewCount,     // 추후 리뷰 도메인 연동
            int studentCount     // 추후 수강신청 도메인 연동
    ) {}
}
