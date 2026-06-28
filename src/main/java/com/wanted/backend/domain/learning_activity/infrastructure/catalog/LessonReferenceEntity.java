package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "lesson")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonReferenceEntity {

    @Id
    private Long id;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
