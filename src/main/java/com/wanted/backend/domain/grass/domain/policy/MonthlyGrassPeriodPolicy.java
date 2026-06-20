package com.wanted.backend.domain.grass.domain.policy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

public class MonthlyGrassPeriodPolicy {

    public MonthlyGrassPeriod calculate(Integer year, Integer month, LocalDate today) {
        if (year == null) {
            throw new IllegalArgumentException("조회 연도는 필수입니다.");
        }
        if (month == null) {
            throw new IllegalArgumentException("조회 월은 필수입니다.");
        }
        if (year < 1) {
            throw new IllegalArgumentException("조회 연도는 1 이상이어야 합니다.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("조회 월은 1~12 사이여야 합니다.");
        }
        if (today == null) {
            throw new IllegalArgumentException("오늘 날짜는 필수입니다.");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDate queryEndDate = calculateQueryEndDate(startDate, endDate, today);

        return new MonthlyGrassPeriod(startDate, endDate, queryEndDate);
    }

    private LocalDate calculateQueryEndDate(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) {
            return startDate.minusDays(1);
        }
        if (today.isAfter(endDate)) {
            return endDate;
        }
        return today;
    }

    public record MonthlyGrassPeriod(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate queryEndDate
    ) {
        public Optional<MonthlyGrassDateRange> queryableDateRange() {
            if (queryEndDate.isBefore(startDate)) {
                return Optional.empty();
            }
            return Optional.of(new MonthlyGrassDateRange(startDate, queryEndDate));
        }
    }

    public record MonthlyGrassDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
