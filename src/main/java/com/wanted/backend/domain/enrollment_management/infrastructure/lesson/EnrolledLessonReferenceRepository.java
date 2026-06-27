package com.wanted.backend.domain.enrollment_management.infrastructure.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EnrolledLessonReferenceRepository extends JpaRepository<EnrolledLessonReferenceEntity, Long> {

    List<EnrolledLessonReferenceEntity> findBySectionIdInOrderByOrderIndexAsc(Collection<Long> sectionIds);
}
