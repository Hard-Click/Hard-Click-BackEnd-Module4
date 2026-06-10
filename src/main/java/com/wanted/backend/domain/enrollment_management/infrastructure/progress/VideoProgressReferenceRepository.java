package com.wanted.backend.domain.enrollment_management.infrastructure.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VideoProgressReferenceRepository extends JpaRepository<VideoProgressReferenceEntity, Long> {

    List<VideoProgressReferenceEntity> findByMemberIdAndCourseIdIn(Long memberId, Collection<Long> courseIds);
}
