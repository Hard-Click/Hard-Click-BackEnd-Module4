package com.wanted.backend.domain.community.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(

        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점 이하이어야 합니다.")
        Integer rating,

        @NotNull(message = "리뷰 내용은 필수입니다.")
        @Size(min = 10, max = 300, message = "리뷰 내용은 10자 이상 300자 이하여야 합니다.")
        String content
) {}