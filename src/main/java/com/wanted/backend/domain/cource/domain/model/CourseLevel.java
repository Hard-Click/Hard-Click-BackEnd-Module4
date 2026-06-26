package com.wanted.backend.domain.cource.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 강의 난이도.
 * <p>
 * API 요청/응답에서는 한글 라벨("입문"/"중급"/"심화")을 그대로 주고받는다(@JsonValue/@JsonCreator).
 * DB에도 라벨 문자열로 저장되어 기존 데이터와 호환된다.
 * 잘못된 값이 들어오면 IllegalArgumentException → 400 으로 처리된다.
 */
public enum CourseLevel {

    BEGINNER("입문"),
    INTERMEDIATE("중급"),
    ADVANCED("심화");

    private final String label;

    CourseLevel(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CourseLevel from(String value) {
        if (value == null) {
            return null;
        }
        for (CourseLevel level : values()) {
            if (level.label.equals(value) || level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 난이도입니다: " + value + " (가능: 입문, 중급, 심화)");
    }
}
