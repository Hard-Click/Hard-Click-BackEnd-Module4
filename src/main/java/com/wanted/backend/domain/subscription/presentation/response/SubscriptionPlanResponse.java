package com.wanted.backend.domain.subscription.presentation.response;

import com.wanted.backend.domain.subscription.application.dto.SubscriptionPlanResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구독 상품(연간 패스) 정보 응답")
public record SubscriptionPlanResponse(
        @Schema(description = "플랜 ID", example = "1")
        Long planId,

        @Schema(description = "플랜명", example = "FLOWN 연간 패스")
        String name,

        @Schema(description = "가격(원)", example = "1580000")
        int price,

        @Schema(description = "이용 기간(일)", example = "365")
        int durationDays,

        @Schema(description = "혜택 목록", example = "[\"모든 유료 강의 수강 가능\"]")
        List<String> benefits
) {
    public static SubscriptionPlanResponse from(SubscriptionPlanResult result) {
        return new SubscriptionPlanResponse(
                result.planId(),
                result.name(),
                result.price(),
                result.durationDays(),
                result.benefits()
        );
    }
}
