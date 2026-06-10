package com.wanted.backend.domain.learning_activity.infrastructure.video;

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
public class VideoReferenceEntity {

    @Id
    @Column(name = "video_id")
    private Long id;

    @Column(name = "curriculum_id", nullable = false)
    private Long curriculumId;
}
