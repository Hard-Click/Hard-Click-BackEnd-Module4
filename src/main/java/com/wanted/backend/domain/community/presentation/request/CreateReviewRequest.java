package com.wanted.backend.domain.community.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(

        @Schema(description = "별점 (1~5 정수)", example = "4")
        @NotNull(message = "별점은 필수입니다.")
        @DecimalMin(value = "1", message = "별점은 최소 0점 이상이어야 합니다.")
        @DecimalMax(value = "5", message = "별점은 최대 5점 이하이어야 합니다.")
        Integer rating,

        @Schema(description = "리뷰 내용 (10자 이상 300자 이하)", example = "강의가 정말 유익했습니다. 실습 위주라 이해가 잘 됐어요.")
        @NotNull(message = "리뷰 내용은 필수입니다.")
        @Size(min = 10, max = 300, message = "리뷰 내용은 10자 이상 300자 이하여야 합니다.")
        String content
) {

}