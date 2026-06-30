package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 등록 응답")
public record CreateReviewResponse(

        @Schema(description = "등록된 리뷰 ID", example = "42")
        Long reviewId
) {
}
