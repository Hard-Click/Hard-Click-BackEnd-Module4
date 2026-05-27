package com.wanted.backend.domain.community.presentation.response;

import java.util.List;


public record CommentListResponse(
        int totalCount,
        List<CommentResponse> comments
) {

}