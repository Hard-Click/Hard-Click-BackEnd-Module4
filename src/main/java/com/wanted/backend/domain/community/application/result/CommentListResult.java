package com.wanted.backend.domain.community.application.result;

import java.util.List;

public record CommentListResult(
        int totalCount,
        List<CommentResult> comments
) {}