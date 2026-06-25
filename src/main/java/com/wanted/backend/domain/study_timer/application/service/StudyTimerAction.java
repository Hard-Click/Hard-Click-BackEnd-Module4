package com.wanted.backend.domain.study_timer.application.service;

public enum StudyTimerAction {

    START("start"),
    PAUSE("pause"),
    HEARTBEAT("heartbeat"),
    END("end"),
    RESUME("resume");

    private final String value;

    StudyTimerAction(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
