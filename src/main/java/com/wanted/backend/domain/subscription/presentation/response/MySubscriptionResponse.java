package com.wanted.backend.domain.subscription.presentation.response;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 구독 상태 응답")
public record MySubscriptionResponse(
        @Schema(description = "구독 중 여부", example = "true")
        boolean subscribed,

        @Schema(description = "구독 ID (미구독 시 null)", example = "300")
        Long subscriptionId,

        @Schema(description = "플랜 ID (미구독 시 null)", example = "1")
        Long planId,

        @Schema(description = "결제 수단 (미구독 시 null)", example = "카드")
        String paymentMethod,

        @Schema(description = "결제 금액 (미구독 시 null)", example = "1580000")
        Integer paidAmount,

        @Schema(description = "구독 시작일 (미구독 시 null)")
        LocalDateTime startedAt,

        @Schema(description = "구독 만료일 (미구독 시 null)")
        LocalDateTime expiredAt,

        @Schema(description = "남은 기간(일)", example = "159")
        long remainingDays
) {
    public static MySubscriptionResponse from(MySubscriptionResult result) {
        return new MySubscriptionResponse(
                result.subscribed(),
                result.subscriptionId(),
                result.planId(),
                result.paymentMethod(),
                result.paidAmount(),
                result.startedAt(),
                result.expiredAt(),
                result.remainingDays()
        );
    }
}
