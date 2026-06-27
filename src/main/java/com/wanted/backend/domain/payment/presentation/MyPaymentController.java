package com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.payment.application.usecase.GetMyPaymentHistoryUseCase;
import com.wanted.backend.domain.payment.presentation.response.MyPaymentHistoryPageResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "Payment", description = "결제 API (/api/payment)")
public class MyPaymentController {

    private final GetMyPaymentHistoryUseCase getMyPaymentHistoryUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "내 결제 내역 조회",
            description = "로그인한 사용자의 결제 내역을 페이지 단위로 조회합니다."
    )
    public ResponseEntity<ApiResponse<MyPaymentHistoryPageResponse>> getMyPayment(
            @Parameter(description = "조회할 페이지 번호. 0부터 시작합니다.", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지에 조회할 결제 내역 수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Page<GetMyPaymentHistoryUseCase.MyPaymentHistoryView> paymentHistories =
                getMyPaymentHistoryUseCase.handle(userDetails.getMemberId(), page, size);

        return ApiResponse.success("결제 내역을 조회했습니다.", MyPaymentHistoryPageResponse.from(paymentHistories));
    }
}
