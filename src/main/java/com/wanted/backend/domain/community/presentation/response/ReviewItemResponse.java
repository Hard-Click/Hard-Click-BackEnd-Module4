package com.wanted.backend.domain.community.presentation.response;

import java.time.LocalDate;

public record ReviewItemResponse(
        Long reviewId,
        String authorName,
        String authorInitial,
        Double rating,
        String content,
        LocalDate createdDate,
        boolean isMyReview
) {}