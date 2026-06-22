package com.wanted.backend.domain.grass.domain.model;

public enum GrassViewMode {

    MONTHLY("monthly"),
    YEARLY("yearly");

    private final String value;

    GrassViewMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
