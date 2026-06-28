package com.wanted.backend.domain.notice.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCourseReferenceRepository
        extends JpaRepository<CourseReferenceEntity, Long> {

    List<CourseReferenceEntity> findByAuthorId(Long authorId);
}