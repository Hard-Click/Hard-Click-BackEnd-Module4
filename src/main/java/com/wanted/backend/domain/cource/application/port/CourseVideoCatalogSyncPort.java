package com.wanted.backend.domain.cource.application.port;

/**
 * 강의 작성 스키마(course_section/lesson)를 영상 재생 스키마(course_curriculum/video)로 미러링한다.
 * 강의 저장·영상 업로드는 lesson에 기록되지만, 재생(learning_activity)은 video 테이블을 읽으므로
 * 두 스키마를 동기화해 업로드한 영상이 실제 재생되도록 한다.
 *
 * 매핑은 PK를 재사용한다: curriculum_id = section.id, video_id = lesson.id.
 */
public interface CourseVideoCatalogSyncPort {

    void syncByCourse(Long courseId);
}
