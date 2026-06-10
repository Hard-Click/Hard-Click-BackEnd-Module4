package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateReviewResponse(

        @Schema(description = "리뷰 등록 응답")
        Long reviewId
) {
}
