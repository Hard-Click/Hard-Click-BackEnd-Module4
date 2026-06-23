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
@Table(name = "video")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogVideoReferenceEntity {

    @Id
    @Column(name = "video_id")
    private Long id;

    @Column(name = "curriculum_id", nullable = false)
    private Long curriculumId;

    @Column(name = "video_url")
    private String streamingUrl;

    @Column(name = "s3_key", nullable = true)
    private String s3Key;

    @Column(name = "is_preview")
    private Boolean preview;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "sort_order")
    private Integer orderIndex;
}
