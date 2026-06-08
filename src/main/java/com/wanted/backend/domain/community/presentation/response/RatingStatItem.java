package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RatingStatItem(

        @Schema(description = "별점 (1~5)", example = "5")
        Integer rating,

        @Schema(description = "해당 별점 리뷰 수", example = "73")
        Long count) {

}