package com.wanted.backend.domain.cource.domain.repository;

import com.wanted.backend.domain.cource.domain.dto.CourseAuthorInfo;
import com.wanted.backend.domain.cource.domain.model.Lesson;

import java.util.Optional;

public interface LessonRepository {
    Lesson save(Lesson lesson);
    Optional<Lesson> findById(Long lessonId);

    // 레슨이 속한 강의의 작성자 ID + 상태 (영상 업로드 권한·삭제 여부 검증용)
    Optional<CourseAuthorInfo> findCourseAuthorInfo(Long lessonId);
}
