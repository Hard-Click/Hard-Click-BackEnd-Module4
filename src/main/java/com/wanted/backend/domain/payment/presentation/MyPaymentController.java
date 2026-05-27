package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.payment.application.usecase.GetMyPaymentHistoryUseCase;
import com.wanted.backend.domain.payment.presentation.response.MyPaymentHistoryResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "Payment", description = "결제 API")
public class MyPaymentController {

    private final GetMyPaymentHistoryUseCase getMyPaymentHistoryUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "내 결제 내역 조회",
            description = "로그인한 사용자의 결제 내역을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<MyPaymentHistoryResponse>>> getMyPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<GetMyPaymentHistoryUseCase.MyPaymentHistoryView> views =
                getMyPaymentHistoryUseCase.handle(userDetails.getMemberId());

        return ApiResponse.success("결제 내역을 조회했습니다.", MyPaymentHistoryResponse.from(views));
    }
}
