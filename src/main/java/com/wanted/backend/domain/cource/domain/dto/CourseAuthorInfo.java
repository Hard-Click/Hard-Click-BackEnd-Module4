package com.wanted.backend.domain.cource.domain.dto;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;

/**
 * 레슨이 속한 강의의 작성자 ID와 상태 (영상 업로드 권한·삭제 여부 검증용)
 */
public record CourseAuthorInfo(
        Long authorId,
        CourseStatus status
) {
    public boolean isDeleted() {
        return status == CourseStatus.DELETED;
    }
}
