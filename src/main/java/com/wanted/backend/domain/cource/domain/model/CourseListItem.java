package com.wanted.backend.domain.cource.domain.model;

import java.time.Instant;

/**
 * 강의 목록 조회용 읽기 모델 (도메인 레이어 순수 Java)
 */
public record CourseListItem(
        Long courseId,
        Long authorId,
        String title,
        String subject,
        String thumbnailUrl,
        PriceType priceType,
        int price,
        Instant createdAt
) {}
