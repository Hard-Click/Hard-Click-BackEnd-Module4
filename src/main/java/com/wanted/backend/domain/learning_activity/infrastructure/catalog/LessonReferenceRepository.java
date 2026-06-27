package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LessonReferenceRepository extends JpaRepository<LessonReferenceEntity, Long> {

    List<LessonReferenceEntity> findBySectionIdInOrderByOrderIndexAscIdAsc(Collection<Long> sectionIds);
}
