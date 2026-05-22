package com.wanted.backend.domain.community.presentation.response;

import java.util.List;

public record ReviewListResponse(
        Double avgRating,
        int totalCount,
        List<RatingStatItem> ratingStats,
        List<ReviewItemResponse> reviews,
        int currentPage,
        int totalPages
) {}