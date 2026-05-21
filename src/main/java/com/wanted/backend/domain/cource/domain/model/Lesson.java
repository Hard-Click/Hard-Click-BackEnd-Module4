package com.wanted.backend.domain.cource.domain.model;

import java.time.Instant;

public class Lesson {

    private Long id;
    private Long sectionId;
    private String title;
    private String description;
    private int orderIndex;
    private String videoUrl;
    private Integer durationSeconds;
    private Instant createdAt;

    private Lesson() {}

    public static Lesson create(Long sectionId, String title, String description,
                                int orderIndex, Instant now) {
        Lesson lesson = new Lesson();
        lesson.sectionId = sectionId;
        lesson.title = title;
        lesson.description = description;
        lesson.orderIndex = orderIndex;
        lesson.createdAt = now;
        return lesson;
    }

    public static Lesson restore(Long id, Long sectionId, String title, String description,
                                 int orderIndex, String videoUrl, Integer durationSeconds,
                                 Instant createdAt) {
        Lesson lesson = new Lesson();
        lesson.id = id;
        lesson.sectionId = sectionId;
        lesson.title = title;
        lesson.description = description;
        lesson.orderIndex = orderIndex;
        lesson.videoUrl = videoUrl;
        lesson.durationSeconds = durationSeconds;
        lesson.createdAt = createdAt;
        return lesson;
    }

    public void attachVideo(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Long getId() { return id; }
    public Long getSectionId() { return sectionId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getOrderIndex() { return orderIndex; }
    public String getVideoUrl() { return videoUrl; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public Instant getCreatedAt() { return createdAt; }
}
