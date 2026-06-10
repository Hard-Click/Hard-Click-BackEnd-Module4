package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SpringDataLessonRepository extends JpaRepository<LessonJpaEntity, Long> {

    // 레슨 → 섹션 → 강의 작성자 ID + 상태 (영상 업로드 권한·삭제 여부 검증용)
    @Query("SELECT new com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo("
            + "l.section.course.authorId, l.section.course.status) "
            + "FROM LessonJpaEntity l WHERE l.id = :lessonId")
    Optional<CourseAuthorInfo> findCourseAuthorInfoByLessonId(@Param("lessonId") Long lessonId);
}
