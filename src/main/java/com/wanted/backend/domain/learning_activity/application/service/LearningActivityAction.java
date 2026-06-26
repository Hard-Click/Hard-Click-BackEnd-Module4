package com.wanted.backend.domain.learning_activity.application.service;

public enum LearningActivityAction {

    VIDEO_ACCESS("videoAccess"),
    COMPLETE_VIDEO("completeVideo"),
    COURSE_PROGRESS("courseProgress");

    private final String value;

    LearningActivityAction(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
