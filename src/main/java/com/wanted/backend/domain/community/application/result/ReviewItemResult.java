package com.wanted.backend.domain.community.application.result;

import java.time.LocalDate;

public record ReviewItemResult(
        Long reviewId,
        String authorName,
        String authorInitial,
        Integer rating,
        String content,
        LocalDate createdDate,
        boolean isMyReview
) {}