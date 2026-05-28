package com.wanted.backend.domain.learning_activity.domain.model;

public class VideoAccessInfo {

    private static final String PUBLISHED = "PUBLISHED";

    private final Long videoId;
    private final Long courseId;
    private final String courseStatus;
    private final Integer coursePrice;
    private final Boolean preview;
    private final String streamingUrl;
    private final Integer durationSeconds;

    public VideoAccessInfo(
            Long videoId,
            Long courseId,
            String courseStatus,
            Integer coursePrice,
            Boolean preview,
            String streamingUrl,
            Integer durationSeconds
    ) {
        this.videoId = videoId;
        this.courseId = courseId;
        this.courseStatus = courseStatus;
        this.coursePrice = coursePrice;
        this.preview = preview;
        this.streamingUrl = streamingUrl;
        this.durationSeconds = durationSeconds;
    }

    public Long videoId() {
        return videoId;
    }

    public Long courseId() {
        return courseId;
    }

    public String courseStatus() {
        return courseStatus;
    }

    public Integer coursePrice() {
        return coursePrice;
    }

    public Boolean preview() {
        return preview;
    }

    public String streamingUrl() {
        return streamingUrl;
    }

    public Integer durationSeconds() {
        return durationSeconds;
    }

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
