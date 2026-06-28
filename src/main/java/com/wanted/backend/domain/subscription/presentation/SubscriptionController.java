package com.wanted.backend.domain.subscription.presentation;

import com.wanted.backend.domain.subscription.application.usecase.CancelSubscriptionUseCase;
import com.wanted.backend.domain.subscription.application.usecase.GetMySubscriptionUseCase;
import com.wanted.backend.domain.subscription.application.usecase.GetSubscriptionPlanUseCase;
import com.wanted.backend.domain.subscription.presentation.response.MySubscriptionResponse;
import com.wanted.backend.domain.subscription.presentation.response.SubscriptionPlanResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구독 신청은 결제 검증을 위해 /api/order/checkout(type=subscription) → /api/payments/confirm
 * 흐름으로만 가능하다. 결제 없이 구독권을 부여하는 POST 엔드포인트는 의도적으로 제공하지 않는다.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "구독(연간 패스) API")
public class SubscriptionController {

    private final GetSubscriptionPlanUseCase getSubscriptionPlanUseCase;
    private final GetMySubscriptionUseCase getMySubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

    @GetMapping("/plan")
    @Operation(summary = "구독 상품 정보 조회", description = "FLOWN 연간 패스의 가격과 혜택 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구독 상품 정보 조회 성공")
    })
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlan() {
        return ApiResponse.success(
                "구독 상품 정보 조회 성공",
                SubscriptionPlanResponse.from(getSubscriptionPlanUseCase.handle())
        );
    }

    @GetMapping("/me")
    @Operation(summary = "내 구독 상태 조회", description = "로그인한 회원의 활성 구독 상태(결제일/남은 기간/결제금액)를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 구독 상태 조회 성공 (미구독 시 subscribed: false)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<MySubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                "내 구독 상태 조회 성공",
                MySubscriptionResponse.from(getMySubscriptionUseCase.handle(userDetails.getMemberId()))
        );
    }

    @DeleteMapping("/me")
    @Operation(summary = "구독 취소", description = "로그인한 회원의 활성 구독을 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구독 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "활성 구독 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 취소된 구독")
    })
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cancelSubscriptionUseCase.handle(userDetails.getMemberId());
        return ApiResponse.successNoContent("구독이 취소되었습니다.");
    }
}
