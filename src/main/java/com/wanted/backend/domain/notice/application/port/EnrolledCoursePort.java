package com.wanted.backend.domain.notice.application.port;

import java.util.List;

public interface EnrolledCoursePort {
    List<Long> getEnrolledCourseIdsByMemberId(Long memberId);
}