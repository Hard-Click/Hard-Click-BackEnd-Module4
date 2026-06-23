package com.wanted.backend.domain.wishlist.presentation;

import com.wanted.backend.domain.wishlist.application.usecase.GetWishlistUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "찜 목록 조회 응답")
public record WishlistResponse(
        @Schema(description = "찜한 강의 목록")
        List<Item> items,
        @Schema(description = "찜한 강의 총 개수", example = "3")
        int totalCount
) {
    @Schema(description = "찜한 강의 항목")
    public record Item(
            @Schema(description = "강의 ID", example = "1")
            Long courseId,
            @Schema(description = "강의명", example = "수능 영어 독해 완성")
            String title,
            @Schema(description = "과목명", example = "수학Ⅱ")
            String subject,
            @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.png")
            String thumbnailUrl,
            @Schema(description = "가격 유형 (FREE / PAID)", example = "PAID")
            String priceType,
            @Schema(description = "강사명", example = "김강사")
            String instructorName,
            @Schema(description = "강의 가격", example = "29000")
            Integer price,
            @Schema(description = "강의 평균 평점", example = "4.5")
            Double averageRating,
            @Schema(description = "리뷰 개수", example = "12")
            Integer reviewCount,
            @Schema(description = "수강생 수", example = "1234")
            Integer enrollmentCount,
            @Schema(description = "수강 여부", example = "false")
            boolean enrolled,
            @Schema(description = "장바구니 포함 여부", example = "true")
            boolean inCart
    ) {}

    public static WishlistResponse from(List<GetWishlistUseCase.Item> useCaseItems) {
        List<Item> items = useCaseItems.stream()
                .map(i -> new Item(
                        i.courseId(),
                        i.title(),
                        i.subject(),
                        i.thumbnailUrl(),
                        i.priceType(),
                        i.instructorName(),
                        i.price(),
                        i.averageRating(),
                        i.reviewCount(),
                        i.enrollmentCount(),
                        i.enrolled(),
                        i.inCart()
                ))
                .toList();
        return new WishlistResponse(items, items.size());
    }
}
