package com.wanted.backend.domain.grass.domain.policy;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

public class YearlyGrassPeriodPolicy {

    public YearlyGrassPeriod calculate(int year, LocalDate today) {
        if (year < 1) {
            throw new IllegalArgumentException("조회 연도는 1 이상이어야 합니다.");
        }
        if (today == null) {
            throw new IllegalArgumentException("오늘 날짜는 필수입니다.");
        }

        LocalDate startDate = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, Month.DECEMBER, 31);
        LocalDate queryEndDate = calculateQueryEndDate(startDate, endDate, today);

        return new YearlyGrassPeriod(startDate, endDate, queryEndDate);
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

    public record YearlyGrassPeriod(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate queryEndDate
    ) {
        public Optional<YearlyGrassDateRange> queryableDateRange() {
            if (queryEndDate.isBefore(startDate)) {
                return Optional.empty();
            }
            return Optional.of(new YearlyGrassDateRange(startDate, queryEndDate));
        }
    }

    public record YearlyGrassDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
