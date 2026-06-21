package com.wanted.backend.domain.grass.domain.policy;

public class LessonGrassLevelPolicy {

    private final int maxLevel;

    public LessonGrassLevelPolicy(int maxLevel) {
        if (maxLevel <= 0) {
            throw new IllegalArgumentException("잔디 최대 레벨은 1 이상이어야 합니다.");
        }
        this.maxLevel = maxLevel;
    }

    public int calculate(int watchedLessonCount) {
        if (watchedLessonCount < 0) {
            throw new IllegalArgumentException("수강량은 0 이상이어야 합니다.");
        }

        return Math.min(watchedLessonCount, maxLevel);
    }
}
