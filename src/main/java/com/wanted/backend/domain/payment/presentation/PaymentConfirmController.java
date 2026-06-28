ackage com.wanted.backend.domain.payment.presentation;

import com.wanted.backend.domain.order.application.usecase.ConfirmOrderPaymentUseCase;
import com.wanted.backend.domain.payment.presentation.request.PaymentConfirmRequest;
import com.wanted.backend.domain.payment.presentation.response.PaymentConfirmResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Payment", description = "결제 API")
public class PaymentConfirmController {

    private final ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;

    @PostMapping("/confirm")
    @Operation(
            summary = "결제 확정",
            description = "Idempotency-Key 헤더 기준으로 동일 요청이 중복 처리되지 않도록 분산락+멱등키를 적용해 결제를 확정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "결제 확정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미 처리된 결제 (멱등 응답)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 금액 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 결제 완료된 주문")
    })
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
