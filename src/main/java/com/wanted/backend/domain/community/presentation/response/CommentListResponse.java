package com.wanted.backend.domain.community.presentation.response;

import com.wanted.backend.domain.community.application.result.CommentListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CommentListResponse(

        @Schema(description = "전체 댓글 수 (대댓글 포함)", example = "15")
        int totalCount,

        @Schema(description = "댓글 목록")
        List<CommentResponse> comments
) {
        public static CommentListResponse from(CommentListResult result) {
                return new CommentListResponse(
                        result.totalCount(),
                        result.comments().stream().map(CommentResponse::from).toList());
        }
}