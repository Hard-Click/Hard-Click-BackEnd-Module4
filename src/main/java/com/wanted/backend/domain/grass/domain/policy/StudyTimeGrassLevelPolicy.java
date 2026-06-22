package com.wanted.backend.domain.grass.domain.policy;

import java.util.List;

public class StudyTimeGrassLevelPolicy {

    private final List<Integer> levelThresholdSeconds;

    public StudyTimeGrassLevelPolicy(List<Integer> levelThresholdSeconds) {
        validate(levelThresholdSeconds);
        this.levelThresholdSeconds = List.copyOf(levelThresholdSeconds);
    }

    public int calculate(int studySeconds) {
        if (studySeconds < 0) {
            throw new IllegalArgumentException("순공시간은 0 이상이어야 합니다.");
        }

        int level = 0;
        for (Integer thresholdSeconds : levelThresholdSeconds) {
            if (studySeconds < thresholdSeconds) {
                return level;
            }
            level++;
        }
        return level;
    }

    private void validate(List<Integer> levelThresholdSeconds) {
        if (levelThresholdSeconds == null || levelThresholdSeconds.isEmpty()) {
            throw new IllegalArgumentException("순공시간 잔디 레벨 기준은 필수입니다.");
        }

        Integer previous = null;
        for (Integer thresholdSeconds : levelThresholdSeconds) {
            if (thresholdSeconds == null || thresholdSeconds <= 0) {
                throw new IllegalArgumentException("순공시간 잔디 레벨 기준은 1초 이상이어야 합니다.");
            }
            if (previous != null && thresholdSeconds <= previous) {
                throw new IllegalArgumentException("순공시간 잔디 레벨 기준은 오름차순이어야 합니다.");
            }
            previous = thresholdSeconds;
        }
    }
}
