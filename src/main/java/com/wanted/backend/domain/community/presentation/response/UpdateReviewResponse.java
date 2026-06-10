package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateReviewResponse(

        @Schema(description = "수정된 리뷰 ID", example = "42")
        Long reviewId
) {

}