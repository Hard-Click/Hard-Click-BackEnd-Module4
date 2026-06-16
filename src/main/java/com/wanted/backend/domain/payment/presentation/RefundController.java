package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.payment.application.command.CourseRefundCommand;
import com.wanted.backend.domain.payment.application.command.SubscriptionRefundCommand;
import com.wanted.backend.domain.payment.application.usecase.GetSubscriptionRefundPreviewUseCase;
import com.wanted.backend.domain.payment.application.usecase.RefundCourseUseCase;
import com.wanted.backend.domain.payment.application.usecase.RefundSubscriptionUseCase;
import com.wanted.backend.domain.payment.presentation.request.CourseRefundRequest;
import com.wanted.backend.domain.payment.presentation.response.CourseRefundResponse;
import com.wanted.backend.domain.payment.presentation.response.SubscriptionRefundPreviewResponse;
import com.wanted.backend.domain.payment.presentation.response.SubscriptionRefundResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment/refund")
@Tag(name = "Refund", description = "환불 API")
public class RefundController {

    private final RefundCourseUseCase refundCourseUseCase;
    private final RefundSubscriptionUseCase refundSubscriptionUseCase;
    private final GetSubscriptionRefundPreviewUseCase getSubscriptionRefundPreviewUseCase;

    @Operation(summary = "단건 강의 환불", description = "결제 후 7일 이내, 진도율 10% 미만인 강의를 환불합니다.")
    @PostMapping("/course")
    public ResponseEntity<ApiResponse<CourseRefundResponse>> refundCourse(
            @Valid @RequestBody CourseRefundRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RefundCourseUseCase.Result result = refundCourseUseCase.handle(
                new CourseRefundCommand(userDetails.getMemberId(), request.courseId(), request.reason())
        );
        return ApiResponse.success("환불이 완료되었습니다.", CourseRefundResponse.from(result));
    }

    @Operation(summary = "구독 환불 예상 금액 조회", description = "남은 이용 기간 기준 일할 계산된 예상 환불 금액을 조회합니다.")
    @GetMapping("/subscription/preview")
    public ResponseEntity<ApiResponse<SubscriptionRefundPreviewResponse>> getSubscriptionRefundPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetSubscriptionRefundPreviewUseCase.Preview preview =
                getSubscriptionRefundPreviewUseCase.handle(userDetails.getMemberId());
        return ApiResponse.success("구독 환불 예상 금액을 조회했습니다.", SubscriptionRefundPreviewResponse.from(preview));
    }

    @Operation(summary = "구독(연간 패스) 환불", description = "활성 구독을 일할 계산으로 환불합니다. 구독 취소 후에도 만료일까지 서비스 이용이 가능합니다.")
    @PostMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionRefundResponse>> refundSubscription(
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RefundSubscriptionUseCase.Result result = refundSubscriptionUseCase.handle(
                new SubscriptionRefundCommand(userDetails.getMemberId(), reason)
        );
        return ApiResponse.success("구독 환불이 완료되었습니다.", SubscriptionRefundResponse.from(result));
    }
}
