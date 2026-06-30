package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 모집 등록 응답")
public record CreateStudyResponse(
        @Schema(description = "생성된 스터디 그룹 ID", example = "45")
        Long groupId,
        @Schema(description = "생성된 스터디 게시글 ID", example = "890")
        Long postId
) {}