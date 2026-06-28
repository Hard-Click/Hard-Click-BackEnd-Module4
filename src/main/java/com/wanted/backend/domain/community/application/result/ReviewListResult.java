package com.wanted.backend.domain.community.application.result;

import java.util.List;

public record ReviewListResult(
        Double avgRating,
        int totalCount,
        List<RatingStatResult> ratingStats,
        List<ReviewItemResult> reviews,
        int currentPage,
        int totalPages
) {}