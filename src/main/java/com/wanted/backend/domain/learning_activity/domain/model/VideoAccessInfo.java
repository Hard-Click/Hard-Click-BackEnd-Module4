package com.wanted.backend.domain.learning_activity.domain.model;

public class VideoAccessInfo {

    private static final String PUBLISHED = "PUBLISHED";

    private final Long videoId;
    private final Long courseId;
    private final String courseStatus;
    private final Integer coursePrice;
    private final Boolean preview;
    private final String s3Key;
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
        this(videoId, courseId, courseStatus, coursePrice, preview, null, streamingUrl, durationSeconds);
    }

    public VideoAccessInfo(
            Long videoId,
            Long courseId,
            String courseStatus,
            Integer coursePrice,
            Boolean preview,
            String s3Key,
            String streamingUrl,
            Integer durationSeconds
    ) {
        this.videoId = videoId;
        this.courseId = courseId;
        this.courseStatus = courseStatus;
        this.coursePrice = coursePrice;
        this.preview = preview;
        this.s3Key = s3Key;
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

    public String s3Key() {
        return s3Key;
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

    public boolean hasS3Key() {
        return s3Key != null && !s3Key.isBlank();
    }

    public boolean hasLegacyStreamingUrl() {
        return streamingUrl != null && !streamingUrl.isBlank();
    }
}
