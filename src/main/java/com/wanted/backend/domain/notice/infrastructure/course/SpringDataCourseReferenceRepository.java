package com.wanted.backend.domain.notice.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataCourseReferenceRepository
        extends JpaRepository<CourseReferenceEntity, Long> {
    @Query("SELECT c.id FROM NoticeCourseReference c WHERE c.authorId = :authorId")
    List<Long> findCourseIdsByAuthorId(@Param("authorId") Long authorId);
}