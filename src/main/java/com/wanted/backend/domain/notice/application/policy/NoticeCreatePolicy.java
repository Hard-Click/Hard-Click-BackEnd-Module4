package com.wanted.backend.domain.notice.application.policy;

import com.wanted.backend.domain.notice.application.port.AdminValidationPort;
import com.wanted.backend.domain.notice.application.port.CourseInstructorPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class NoticeCreatePolicy {

    private final CourseInstructorPort courseInstructorPort;
    private final AdminValidationPort adminValidationPort;

    public NoticeCreatePolicy(CourseInstructorPort courseInstructorPort, AdminValidationPort adminValidationPort) {
        this.courseInstructorPort = courseInstructorPort;
        this.adminValidationPort = adminValidationPort;
    }

    public void validate(Long memberId, Long courseId) {
        Long courseInstructorId = courseInstructorPort.getInstructorIdByCourseId(courseId);

        if (adminValidationPort.isAdmin(memberId)) {
            return;
        }

        if (!courseInstructorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
        }
    }
}