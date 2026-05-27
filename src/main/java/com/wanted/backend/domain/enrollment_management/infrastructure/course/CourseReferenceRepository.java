package com.wanted.backend.domain.enrollment_management.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CourseReferenceRepository extends JpaRepository<CourseReferenceEntity, Long> {

    List<CourseReferenceEntity> findByIdIn(Collection<Long> courseIds);
}
