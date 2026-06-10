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
@Table(name = "lesson")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoReferenceEntity {

    @Id
    @Column(name = "id")
    private Long id;

    // section_id = FK to course_section.id
    @Column(name = "section_id", nullable = false)
    private Long curriculumId;
}
