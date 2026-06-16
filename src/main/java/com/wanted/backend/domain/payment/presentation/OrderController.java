package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.payment.application.command.CreateCourseOrderCommand;
import com.wanted.backend.domain.payment.application.command.CreateSubscriptionOrderCommand;
import com.wanted.backend.domain.payment.application.usecase.CreateCourseOrderUseCase;
import com.wanted.backend.domain.payment.application.usecase.CreateSubscriptionOrderUseCase;
import com.wanted.backend.domain.payment.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.payment.presentation.request.CreateCourseOrderRequest;
import com.wanted.backend.domain.payment.presentation.request.CreateSubscriptionOrderRequest;
import com.wanted.backend.domain.payment.presentation.response.CreateCourseOrderResponse;
import com.wanted.backend.domain.payment.presentation.response.CreateSubscriptionOrderResponse;
import com.wanted.backend.domain.payment.presentation.response.GetOrderResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    private final CreateCourseOrderUseCase createCourseOrderUseCase;
    private final CreateSubscriptionOrderUseCase createSubscriptionOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    @Operation(summary = "단건 강의 주문 생성", description = "선택한 강의들로 주문을 생성합니다. Toss 결제 전에 호출해주세요.")
    @PostMapping("/course")
    public ResponseEntity<ApiResponse<CreateCourseOrderResponse>> createCourseOrder(
            @Valid @RequestBody CreateCourseOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CreateCourseOrderUseCase.Result result = createCourseOrderUseCase.handle(
                new CreateCourseOrderCommand(userDetails.getMemberId(), request.courseIds()));
        return ApiResponse.success("주문이 생성되었습니다.", CreateCourseOrderResponse.from(result));
    }

    @Operation(summary = "구독권 주문 생성", description = "구독 플랜으로 주문을 생성합니다. Toss 결제 전에 호출해주세요.")
    @PostMapping("/subscription")
    public ResponseEntity<ApiResponse<CreateSubscriptionOrderResponse>> createSubscriptionOrder(
            @Valid @RequestBody CreateSubscriptionOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CreateSubscriptionOrderUseCase.Result result = createSubscriptionOrderUseCase.handle(
                new CreateSubscriptionOrderCommand(userDetails.getMemberId(), request.planId()));
        return ApiResponse.success("구독 주문이 생성되었습니다.", CreateSubscriptionOrderResponse.from(result));
    }

    @Operation(summary = "주문 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<GetOrderResponse>> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetOrderUseCase.Result result = getOrderUseCase.handle(orderId, userDetails.getMemberId());
        return ApiResponse.success("주문 정보를 조회했습니다.", GetOrderResponse.from(result));
    }
}
