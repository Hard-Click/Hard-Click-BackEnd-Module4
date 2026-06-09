package com.wanted.backend.domain.cource.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SpringDataLessonRepository extends JpaRepository<LessonJpaEntity, Long> {

    // 레슨 → 섹션 → 강의 작성자 ID (영상 업로드 권한 검증용)
    @Query("SELECT l.section.course.authorId FROM LessonJpaEntity l WHERE l.id = :lessonId")
    Optional<Long> findCourseAuthorIdByLessonId(@Param("lessonId") Long lessonId);
}
