package com.wanted.backend.domain.notice.application.port;

import java.util.List;

// 강사별 담당 강의 ID 목록 조회 포트
public interface InstructorCoursePort {
    List<Long> getCourseIdsByInstructorId(Long memberId);
}