package com.wanted.backend.domain.community.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(

        @NotNull(message = "리뷰를 작성할 강의 ID는 필수입니다.")
        Long courseId,

        @NotNull(message = "별점은 필수입니다.")
        @DecimalMin(value = "0.5", message = "별점은 최소 0.5점 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 최대 5.0점 이하이어야 합니다.")
        Double rating,

        @NotNull(message = "리뷰 내역은 필수입니다.")
        @Size(min = 10, max = 300, message = "리뷰 내용은 10자 이상 300자 이하여야 합니다.")
        String content
) {
}
