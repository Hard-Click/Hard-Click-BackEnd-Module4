package com.wanted.backend.domain.order.presentation;

import com.wanted.backend.domain.order.application.usecase.CheckoutUseCase;
import com.wanted.backend.domain.order.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.order.application.usecase.RefundOrderItemUseCase;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.presentation.response.CheckoutResponse;
import com.wanted.backend.domain.order.presentation.response.OrderDetailResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API (결제 진입/주문 상세/환불)")
public class OrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final RefundOrderItemUseCase refundOrderItemUseCase;

    @GetMapping("/checkout")
    @Operation(
            summary = "결제 진입(주문 준비)",
            description = "type=course(강의/장바구니) 또는 subscription(연간 패스)로 READY 주문을 생성합니다. " +
                    "course 단건 결제 시 courseId를 함께 전달하고, 생략 시 장바구니 전체로 처리합니다."
    )
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 타입 (course | subscription)", example = "course")
            @RequestParam String type,
            @Parameter(description = "단건 강의 결제 대상 ID (course 단건 결제 시)", example = "1")
            @RequestParam(required = false) Long courseId
    ) {
        OrderType orderType = parseType(type);
        CheckoutResponse response = CheckoutResponse.from(
                checkoutUseCase.checkout(userDetails.getMemberId(), orderType, courseId));
        return ApiResponse.success("결제 진입 성공", response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "주문 상세/결제 내역/환불 화면용. 본인 주문만 조회 가능합니다.")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        OrderDetailResponse response = OrderDetailResponse.from(
                getOrderUseCase.getOrder(userDetails.getMemberId(), orderId));
        return ApiResponse.success("주문 상세 조회 성공", response);
    }

    @PostMapping("/{orderId}/items/{courseId}/refund")
    @Operation(
            summary = "주문 항목 환불",
            description = "결제 완료된 주문의 특정 강의 항목을 환불합니다. 본인 주문만 가능하며 " +
                    "Toss 결제취소 API 호출 후 수강 권한이 즉시 박탈되고 주문/항목 상태가 갱신됩니다. " +
                    "Idempotency-Key 헤더 기준으로 동일 요청 재시도가 안전하게 처리됩니다."
    )
    public ResponseEntity<ApiResponse<Void>> refundOrderItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @PathVariable Long courseId,
            @Parameter(description = "클라이언트가 생성한 멱등키(UUID v4). 동일 키로 재요청 시 동일 결과를 반환합니다.")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String normalizedIdempotencyKey = idempotencyKey.trim();
        try {
            UUID.fromString(normalizedIdempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        refundOrderItemUseCase.refund(userDetails.getMemberId(), orderId, courseId, normalizedIdempotencyKey);
        return ApiResponse.successNoContent("환불이 처리되었습니다.");
    }

    private OrderType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        try {
            return OrderType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
