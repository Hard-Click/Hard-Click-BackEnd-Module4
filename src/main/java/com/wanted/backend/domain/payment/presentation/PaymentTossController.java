package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.payment.application.command.ConfirmPaymentCommand;
import com.wanted.backend.domain.payment.application.usecase.ConfirmPaymentUseCase;
import com.wanted.backend.domain.payment.presentation.request.ConfirmPaymentRequest;
import com.wanted.backend.domain.payment.presentation.response.ConfirmPaymentResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/toss")
@Tag(name = "Payment", description = "Toss 결제 API")
public class PaymentTossController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Operation(summary = "Toss 결제 승인", description = "Toss Payments 결제 승인을 처리합니다. 프론트에서 Toss SDK 결제 완료 후 호출해주세요.")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<ConfirmPaymentResponse>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ConfirmPaymentUseCase.Result result = confirmPaymentUseCase.handle(
                new ConfirmPaymentCommand(
                        userDetails.getMemberId(),
                        request.paymentKey(),
                        request.orderId(),
                        request.amount()
                )
        );
        return ApiResponse.success("결제가 완료되었습니다.", ConfirmPaymentResponse.from(result));
    }
}
