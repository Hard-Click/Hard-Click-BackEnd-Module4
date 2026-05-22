package com.wanted.backend.domain.learning_activity.domain.model;

/* comment.
*   비즈니스 판단에 필요한 순수 데이터와 메서드를 가짐
*   Course 를 직접 만들지 않기 위해 만든 조회 모델
*   Course 도메인을 침범하지 않고, 필요한 값만 읽어온 모델
* */

public record VideoAccessInfo(
        Long videoId,
        Long courseId,
        String courseStatus,
        Integer coursePrice,
        Boolean preview,
        String streamingUrl,
        Integer durationSeconds
) {

    private static final String PUBLISHED = "PUBLISHED";

    public boolean isPublishedCourse() {
        return PUBLISHED.equals(courseStatus);
    }

    public boolean isFreeCourse() {
        return coursePrice != null && coursePrice == 0;
    }

    public boolean isPreview() {
        return Boolean.TRUE.equals(preview);
    }
}
