package com.wanted.backend.domain.order.presentation;

import com.wanted.backend.domain.order.application.usecase.CheckoutUseCase;
import com.wanted.backend.domain.order.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.order.application.usecase.RefundOrderItemUseCase;
import com.wanted.backend.domain.order.application.usecase.RefundSubscriptionUseCase;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.presentation.response.CheckoutResponse;
import com.wanted.backend.domain.order.presentation.response.OrderDetailResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API (결제 진입/주문 상세/환불)")
public class OrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final RefundOrderItemUseCase refundOrderItemUseCase;
    private final RefundSubscriptionUseCase refundSubscriptionUseCase;

    @GetMapping("/checkout")
    @Operation(
            summary = "결제 진입(주문 준비)",
            description = "type=course(강의/장바구니) 또는 subscription(연간 패스)로 READY 주문을 생성합니다. " +
                    "course 단건 결제 시 courseId를 전달하고, " +
                    "courseIds 전달 시 선택한 장바구니 강의만 결제하며, " +
                    "둘 다 없으면 장바구니 전체를 결제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 준비 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 타입 또는 입력값"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "강의 또는 장바구니 항목을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 결제 완료된 강의가 포함된 경우")
    })
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "주문 타입 (course | subscription)", example = "course")
            @RequestParam String type,

            @Parameter(description = "단건 강의 결제 대상 ID", example = "1")
            @RequestParam(required = false) Long courseId,

            @Parameter(description = "선택한 장바구니 강의 ID 목록", example = "1,2,3")
            @RequestParam(required = false) List<Long> courseIds
    ) {
        OrderType orderType = parseType(type);

        CheckoutResponse response = CheckoutResponse.from(
                checkoutUseCase.checkout(
                        userDetails.getMemberId(),
                        orderType,
                        courseId,
                        courseIds
                )
        );

        return ApiResponse.success("결제 진입 성공", response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "주문 상세/결제 내역/환불 화면용. 본인 주문만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 주문이 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 주문 ID", example = "101")
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "환불 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 멱등키 형식"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 주문이 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "주문 또는 강의 항목을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 환불된 항목이거나 환불 불가 상태")
    })
    public ResponseEntity<ApiResponse<Void>> refundOrderItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "환불할 주문 ID", example = "101")
            @PathVariable Long orderId,
            @Parameter(description = "환불할 강의 ID", example = "1")
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

        refundOrderItemUseCase.refund(
                userDetails.getMemberId(),
                orderId,
                courseId,
                normalizedIdempotencyKey
        );

        return ApiResponse.successNoContent("환불이 처리되었습니다.");
    }

    @PostMapping("/{orderId}/refund")
    @Operation(
            summary = "구독 환불",
            description = "결제 완료된 구독(연간 패스) 주문을 전액 환불합니다. 본인 주문만 가능하며 " +
                    "Toss 결제취소 API 호출 후 구독이 즉시 취소되고 주문 상태가 REFUNDED로 갱신됩니다. " +
                    "Idempotency-Key 헤더 기준으로 동일 요청 재시도가 안전하게 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 환불 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 멱등키 형식"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "본인 주문이 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "구독 주문이 아니거나 환불 불가 상태")
    })
    public ResponseEntity<ApiResponse<Void>> refundSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "환불할 구독 주문 ID", example = "201")
            @PathVariable Long orderId,
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

        refundSubscriptionUseCase.refund(
                userDetails.getMemberId(),
                orderId,
                normalizedIdempotencyKey
        );

        return ApiResponse.successNoContent("구독 환불이 처리되었습니다.");
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