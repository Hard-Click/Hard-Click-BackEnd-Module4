package com.wanted.backend.domain.order.infrastructure.subscription;

import com.wanted.backend.domain.order.application.port.OrderSubscriptionPlanPort;
import org.springframework.stereotype.Component;

/**
 * 연간 패스 상품 정보 제공.
 * 현재 develop 기준 브랜치라 subscription 도메인 카탈로그가 없어 로컬 상수로 둔다.
 * (Stage 4에서 subscription 도메인의 SubscriptionPlanCatalog로 통합 예정)
 */
@Component
public class OrderSubscriptionPlanAdapter implements OrderSubscriptionPlanPort {

    private static final Long ANNUAL_PASS_PLAN_ID = 1L;
    private static final String ANNUAL_PASS_NAME = "FLOWN 연간 패스";
    private static final int ANNUAL_PASS_PRICE = 1_580_000;

    @Override
    public PlanInfo getAnnualPass() {
        return new PlanInfo(ANNUAL_PASS_PLAN_ID, ANNUAL_PASS_NAME, ANNUAL_PASS_PRICE);
    }
}
