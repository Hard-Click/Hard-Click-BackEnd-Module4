package com.wanted.backend.domain.grass.domain.policy;

public class GrassLearningStatusPolicy {

    public boolean hasStudyRecord(Integer watchedLessonCount, Integer studySeconds) {
        return isPositive(watchedLessonCount) || isPositive(studySeconds);
    }

    private boolean isPositive(Integer value) {
        return value != null && value > 0;
    }
}
