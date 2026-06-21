package com.wanted.backend.domain.ranking.domain.model;

public enum RankingMetric {

    STUDY_TIME("study-time"),
    LESSON("lessons"),
    ACCEPTED_COMMENT("accepted-comments");

    private final String key;

    RankingMetric(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
