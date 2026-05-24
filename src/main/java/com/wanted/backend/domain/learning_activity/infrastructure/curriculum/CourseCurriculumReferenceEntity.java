package com.wanted.backend.domain.learning_activity.infrastructure.curriculum;

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
@Table(name = "course_curriculum") 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseCurriculumReferenceEntity {

    @Id
    @Column(name = "curriculum_id")
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;
}
