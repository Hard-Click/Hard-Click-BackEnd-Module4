package com.wanted.backend.domain.learning_activity.infrastructure.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VideoReferenceRepository extends JpaRepository<VideoReferenceEntity, Long> {

    List<VideoReferenceEntity> findByCurriculumIdInOrderByIdAsc(Collection<Long> curriculumIds);
}
