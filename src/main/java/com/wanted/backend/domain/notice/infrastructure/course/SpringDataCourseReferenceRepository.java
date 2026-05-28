package com.wanted.backend.domain.notice.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourseReferenceRepository
        extends JpaRepository<CourseReferenceEntity, Long> {

}