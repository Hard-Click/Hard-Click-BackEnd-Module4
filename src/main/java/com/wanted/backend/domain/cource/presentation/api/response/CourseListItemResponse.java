package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.model.PriceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "강의 목록 항목")
public record CourseListItemResponse(
        @Schema(description = "강의 ID", example = "1")
        Long courseId,

        @Schema(description = "강의명", example = "2027 수능 수학Ⅱ 미적분 실전 킬러 특강")
        String title,

        @Schema(description = "과목명", example = "수학Ⅱ")
        String subjectName,

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.png")
        String thumbnailUrl,

        @Schema(description = "가격 표시 (무료 or N원)", example = "89,000원")
        String priceLabel,

        @Schema(description = "가격 유형 (FREE / PAID)", example = "PAID")
        PriceType priceType,

        @Schema(description = "가격 (원)", example = "89000")
        int price,

        @Schema(description = "강사명", example = "박지훈")
        String instructorName,

        @Schema(description = "평균 평점", example = "4.8")
        double averageRating,

        @Schema(description = "리뷰 수", example = "1234")
        int reviewCount,

        @Schema(description = "수강생 수", example = "12543")
        int studentCount,

        @Schema(description = "등록일시")
        Instant createdAt,

        @Schema(description = "강의 상태 (DRAFT / PUBLISHED)", example = "PUBLISHED")
        CourseStatus status
) {
    public static CourseListItemResponse from(CourseListResult.Item item) {
        String priceLabel = item.priceType() == PriceType.FREE
                ? "무료"
                : String.format("%,d원", item.price());
        return new CourseListItemResponse(
                item.courseId(), item.title(), item.subject(), item.thumbnailUrl(),
                priceLabel, item.priceType(), item.price(),
                item.instructorName(), item.rating(), item.reviewCount(), item.studentCount(),
                item.createdAt(), item.status()
        );
    }
}
