package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "통합 게시판 목록 응답 (게시글 + 스터디)")
public record UnifiedBoardListResponse(
        @Schema(description = "통합 게시판 항목 목록")
        List<UnifiedBoardItemResponse> items,
        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        int currentPage,
        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,
        @Schema(description = "전체 항목 수", example = "48")
        long totalCount
) {}