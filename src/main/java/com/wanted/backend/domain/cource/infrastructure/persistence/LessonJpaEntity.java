package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "lesson")
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

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_processing_status")
    private FileProcessingStatus fileProcessingStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    static LessonJpaEntity of(CourseSectionJpaEntity section, String title,
                               String description, int orderIndex,
                               Integer durationSeconds, Instant createdAt) {
        LessonJpaEntity entity = new LessonJpaEntity();
        entity.section = section;
        entity.title = title;
        entity.description = description;
        entity.orderIndex = orderIndex;
        entity.durationSeconds = durationSeconds;
        entity.createdAt = createdAt;
        return entity;
    }

    void update(String videoUrl, String s3Key, FileProcessingStatus status) {
        if (videoUrl != null) this.videoUrl = videoUrl;
        if (s3Key != null) this.s3Key = s3Key;
        if (status != null) this.fileProcessingStatus = status;
    }

    // 수정 시 videoUrl/durationSeconds/fileProcessingStatus 는 보존
    void updateMeta(String title, String description, int orderIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}
