package com.wanted.backend.domain.ranking.domain.model;

public enum RankingPeriod {

    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    RankingPeriod(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
