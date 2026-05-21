package com.wanted.backend.domain.cource.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "lessons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSectionJpaEntity section;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    static LessonJpaEntity of(CourseSectionJpaEntity section, String title,
                               String description, int orderIndex, Instant createdAt) {
        LessonJpaEntity entity = new LessonJpaEntity();
        entity.section = section;
        entity.title = title;
        entity.description = description;
        entity.orderIndex = orderIndex;
        entity.createdAt = createdAt;
        return entity;
    }

    void attachVideo(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
