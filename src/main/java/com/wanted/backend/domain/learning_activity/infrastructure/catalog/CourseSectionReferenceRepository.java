package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionReferenceRepository extends JpaRepository<CourseSectionReferenceEntity, Long> {

    List<CourseSectionReferenceEntity> findByCourseIdOrderByOrderIndexAscIdAsc(Long courseId);
}
