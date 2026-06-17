package com.wanted.backend.domain.notice.application.port;

import java.util.List;

public interface InstructorCoursePort {
    List<Long> getCourseIdsByInstructorId(Long memberId);
}