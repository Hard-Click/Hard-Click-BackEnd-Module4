package com.wanted.backend.domain.learning_activity.infrastructure.curriculum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseCurriculumReferenceRepository extends JpaRepository<CourseCurriculumReferenceEntity, Long> {

    List<CourseCurriculumReferenceEntity> findByCourseIdOrderByIdAsc(Long courseId);
}
