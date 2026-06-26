package com.wanted.backend.domain.order.presentation.response;

import com.wanted.backend.domain.order.application.dto.CheckoutResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "결제 진입(주문 준비) 응답")
public record CheckoutResponse(
        @Schema(description = "주문 번호", example = "ORD-20260520-1A2B3C4D")
        String orderNo,

        @Schema(description = "주문 타입 (COURSE / SUBSCRIPTION)", example = "COURSE")
        String type,

        @Schema(description = "주문 상태", example = "READY")
        String status,

        @Schema(description = "결제 대상 항목")
        List<Item> items,

        @Schema(description = "총 금액", example = "89000")
        int totalAmount,

        @Schema(description = "최종 결제 금액", example = "89000")
        int finalAmount
) {
    @Schema(description = "결제 대상 항목")
    public record Item(
            @Schema(description = "강의 ID (구독은 null)", example = "1")
            Long courseId,
            @Schema(description = "제목", example = "2027 수능 수학 미적분 실전 킬러 특강")
            String title,
            @Schema(description = "가격", example = "89000")
            int price
    ) {}

    public static CheckoutResponse from(CheckoutResult result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.title(), i.price()))
                .toList();
        return new CheckoutResponse(
                result.orderNo(),
                result.type().name(),
                result.status().name(),
                items,
                result.totalAmount(),
                result.finalAmount()
        );
    }
}
