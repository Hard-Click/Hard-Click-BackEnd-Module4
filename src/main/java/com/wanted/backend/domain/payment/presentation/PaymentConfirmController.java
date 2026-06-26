package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.order.application.usecase.ConfirmOrderPaymentUseCase;
import com.wanted.backend.domain.payment.presentation.request.PaymentConfirmRequest;
import com.wanted.backend.domain.payment.presentation.response.PaymentConfirmResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 확정 API (중복결제 방지)")
public class PaymentConfirmController {

    private final ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;

    @PostMapping("/confirm")
    @Operation(
            summary = "결제 확정",
            description = "Idempotency-Key 헤더 기준으로 동일 요청이 중복 처리되지 않도록 분산락+멱등키를 적용해 결제를 확정합니다."
    )
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirm(
            @Valid @RequestBody PaymentConfirmRequest request,
            @Parameter(description = "클라이언트가 생성한 멱등키(UUID v4). 동일 키로 재요청 시 동일 결과를 반환합니다.")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal CustomUserDetails userDetails
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

        ConfirmOrderPaymentUseCase.Result result = confirmOrderPaymentUseCase.confirm(
                userDetails.getMemberId(),
                request.orderId(),
                request.paymentKey(),
                request.amount(),
                normalizedIdempotencyKey
        );

        PaymentConfirmResponse response = PaymentConfirmResponse.from(result);
        return result.duplicate()
                ? ApiResponse.success("이미 처리된 결제 요청입니다.", response)
                : ApiResponse.created("결제가 확정되었습니다.", response);
    }
}
