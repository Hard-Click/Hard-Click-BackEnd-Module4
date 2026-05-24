package com.wanted.backend.domain.community.presentation.response;

import java.util.List;

public record PostListResponse(
        List<PostItemResponse> posts,
        int currentPage,
        int totalPages,
        int totalCount
) {}