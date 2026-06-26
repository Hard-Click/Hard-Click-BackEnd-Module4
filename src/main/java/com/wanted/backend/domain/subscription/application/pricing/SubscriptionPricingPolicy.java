package com.wanted.backend.domain.subscription.application.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 연간 패스 동적 가격 정책.
 * <p>
 * 가격 = (다가오는 수능까지 남은 일수 D-day) × 일일 단가(daily-rate).
 * 수능이 가까울수록 가격이 내려간다. 수능 당일/경과 시에는 다음 해 수능을 기준으로
 * 자동 롤오버하므로 D-day는 항상 1 이상(가격이 0/음수가 되지 않음)이다.
 */
@Component
public class SubscriptionPricingPolicy {

    private final Clock clock;
    private final LocalDate baseSuneungDate;
    private final int dailyRate;

    public SubscriptionPricingPolicy(
            Clock clock,
            @Value("${subscription.suneung-date:2026-11-19}") String suneungDate,
            @Value("${subscription.daily-rate:30000}") int dailyRate) {
        this.clock = clock;
        this.baseSuneungDate = LocalDate.parse(suneungDate);
        this.dailyRate = dailyRate;
    }

    /**
     * 오늘 기준 다가오는(미래) 수능 날짜.
     * 설정된 수능 날짜가 이미 지났으면 다음 해 같은 날짜로 롤오버한다.
     */
    public LocalDate upcomingSuneungDate() {
        LocalDate today = LocalDate.now(clock);
        LocalDate target = baseSuneungDate;
        while (!target.isAfter(today)) {
            target = target.plusYears(1);
        }
        return target;
    }

    /** 다가오는 수능까지 남은 일수(D-day). 항상 1 이상. */
    public long daysUntilSuneung() {
        return ChronoUnit.DAYS.between(LocalDate.now(clock), upcomingSuneungDate());
    }

    /** 연간 패스 현재가 = D-day × 일일 단가. */
    public int currentPrice() {
        return Math.toIntExact(daysUntilSuneung() * (long) dailyRate);
    }
}
