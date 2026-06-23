package com.wanted.backend.domain.cart.presentation.response;

import com.wanted.backend.domain.cart.application.usecase.GetCartUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "장바구니 조회 응답")
public record CartResponse(
        @Schema(description = "장바구니 강의 목록")
        List<Item> items,
        @Schema(description = "선택된 강의 수", example = "2")
        int selectedCount,
        @Schema(description = "결제 예정 총 금액", example = "59000")
        Integer totalAmount
) {
    @Schema(description = "장바구니 강의 항목")
    public record Item(
            @Schema(description = "강의 ID", example = "1")
            Long courseId,
            @Schema(description = "강의명", example = "수능 영어 독해 완성")
            String title,
            @Schema(description = "강사명", example = "김강사")
            String instructorName,
            @Schema(description = "강의 가격", example = "29000")
            Integer price
    ) {}

    public static CartResponse from(GetCartUseCase.Result result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.title(), i.instructorName(), i.price()))
                .toList();
        return new CartResponse(items, result.selectedCount(), result.totalAmount());
    }
}
