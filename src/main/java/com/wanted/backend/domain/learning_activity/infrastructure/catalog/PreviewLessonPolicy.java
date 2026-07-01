package com.wanted.backend.domain.learning_activity.infrastructure.catalog;

/**
 * 미리보기 레슨 판정 기준을 한 곳에서 관리한다 — 재생 권한(VideoCatalogAdapter)과 진도 집계
 * (CourseProgressQueryAdapter)가 같은 기준을 써야 두 경로가 어긋나지 않는다.
 */
public final class PreviewLessonPolicy {

    private PreviewLessonPolicy() {
    }

    public static boolean isPreview(CourseSectionReferenceEntity section, LessonReferenceEntity lesson) {
        return section.getOrderIndex() == 0 && lesson.getOrderIndex() == 0;
    }
}
