package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ReviewItemResponse(

        @Schema(description = "리뷰 ID", example = "42")
        Long reviewId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성자 이름 첫 글자 (아바타용)", example = "홍")
        String authorInitial,

        @Schema(description = "별점 (1~5)", example = "5")
        Integer rating,

        @Schema(description = "리뷰 내용", example = "강의가 정말 유익했습니다.")
        String content,

        @Schema(description = "작성일", example = "2025-03-15")
        LocalDate createdDate,

        @Schema(description = "본인이 작성한 리뷰 여부", example = "false")
        boolean isMyReview
) {

}