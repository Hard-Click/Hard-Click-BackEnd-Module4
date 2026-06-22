package com.wanted.backend.domain.community.presentation.response;

import java.util.List;

public record UnifiedBoardListResponse(
        List<UnifiedBoardItemResponse> items,
        int currentPage,
        int totalPages,
        long totalCount
) {}