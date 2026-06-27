package com.wanted.backend.domain.order.presentation.response;

import com.wanted.backend.domain.order.application.dto.OrderDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 상세 응답")
public record OrderDetailResponse(

        @Schema(description = "주문 번호", example = "ORD-20260520-1A2B3C4D")
        String orderNo,

        @Schema(description = "주문 상태 (READY/PAID/PARTIAL_REFUNDED/REFUNDED/CANCELED)", example = "PAID")
        String status,

        @Schema(description = "결제 타입 (COURSE / SUBSCRIPTION)", example = "COURSE")
        String paymentType,

        @Schema(description = "주문 일시")
        LocalDateTime orderedAt,

        @Schema(description = "결제 완료 일시 (미결제 시 null)")
        LocalDateTime paidAt,

        @Schema(description = "주문 항목")
        List<Item> items,

        @Schema(description = "총 금액", example = "89000")
        int totalAmount
) {

    @Schema(description = "주문 항목")
    public record Item(

            @Schema(description = "강의 ID (구독은 null)", example = "1")
            Long courseId,

            @Schema(description = "제목", example = "2027 수능 수학 미적분 실전 킬러 특강")
            String title,

            @Schema(description = "썸네일 URL (구독은 null)")
            String thumbnailUrl,

            @Schema(description = "가격", example = "89000")
            int price,

            @Schema(description = "환불 가능 여부", example = "true")
            boolean refundable,

            @Schema(description = "환불 가능 금액", example = "89000")
            int refundAmount,

            @Schema(description = "이미 환불된 항목 여부", example = "false")
            boolean refunded,

            @Schema(description = "수강 상태 (구독은 null)", example = "IN_PROGRESS")
            String enrollStatus
    ) {
    }

    public static OrderDetailResponse from(OrderDetailResult result) {

        List<Item> items = result.items().stream()
                .map(i -> new Item(
                        i.courseId(),
                        i.title(),
                        i.thumbnailUrl(),
                        i.price(),
                        i.refundable(),
                        i.refundAmount(),
                        i.refunded(),
                        i.enrollStatus()
                ))
                .toList();

        return new OrderDetailResponse(
                result.orderNo(),
                result.status().name(),
                result.paymentType().name(),
                result.orderedAt(),
                result.paidAt(),
                items,
                result.totalAmount()
        );
    }
}