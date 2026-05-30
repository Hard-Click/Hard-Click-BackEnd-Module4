package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostListResponse(

        @Schema(description = "게시글 목록")
        List<PostItemResponse> posts,

        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "전체 게시글 수", example = "48")
        int totalCount
) {}