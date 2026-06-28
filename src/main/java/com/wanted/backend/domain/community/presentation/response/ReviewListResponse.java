package com.wanted.backend.domain.community.presentation.response;

import com.wanted.backend.domain.community.application.result.ReviewListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ReviewListResponse(

        @Schema(description = "별점 평균", example = "4.3")
        Double avgRating,

        @Schema(description = "전체 리뷰 수", example = "128")
        int totalCount,

        @Schema(description = "별점 분포 (1~5점별 개수)")
        List<RatingStatItem> ratingStats,

        @Schema(description = "리뷰 목록")
        List<ReviewItemResponse> reviews,

        @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages
) {
        public static ReviewListResponse from(ReviewListResult result) {
                return new ReviewListResponse(
                        result.avgRating(), result.totalCount(),
                        result.ratingStats().stream().map(RatingStatItem::from).toList(),
                        result.reviews().stream().map(ReviewItemResponse::from).toList(),
                        result.currentPage(), result.totalPages());
        }
}