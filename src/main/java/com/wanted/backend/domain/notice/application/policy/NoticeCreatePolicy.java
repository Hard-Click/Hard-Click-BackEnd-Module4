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

        if (adminValidationPort.isAdmin(memberId)) {
            return;
        }

        // [1단계] 강의 담당 강사 ID 조회 (존재하지 않는 강의면 예외 발생)
        Long courseInstructorId = courseInstructorPort.getInstructorIdByCourseId(courseId);

        // [2단계] 해당 강의의 담당 강사인지 확인
        if (!courseInstructorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
        }
    }
}