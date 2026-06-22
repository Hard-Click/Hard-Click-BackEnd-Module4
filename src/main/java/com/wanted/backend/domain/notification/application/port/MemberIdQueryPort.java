package com.wanted.backend.domain.notification.application.port;

import java.util.List;

public interface MemberIdQueryPort {
    List<Long> findAllAdminIds();
    List<Long> findAllInstructorIds();
    List<Long> findAllStudentIds();
}