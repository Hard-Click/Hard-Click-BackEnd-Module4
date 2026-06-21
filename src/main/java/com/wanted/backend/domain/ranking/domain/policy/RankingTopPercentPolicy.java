package com.wanted.backend.domain.ranking.domain.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RankingTopPercentPolicy {

    public Double calculate(Long rank, Long totalUsers) {
        if (rank == null || totalUsers == null || totalUsers <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(rank)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalUsers), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
