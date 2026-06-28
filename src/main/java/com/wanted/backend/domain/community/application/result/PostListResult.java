package com.wanted.backend.domain.community.application.result;

import java.util.List;

public record PostListResult(
        List<PostItemResult> posts,
        int currentPage,
        int totalPages,
        int totalCount
) {}