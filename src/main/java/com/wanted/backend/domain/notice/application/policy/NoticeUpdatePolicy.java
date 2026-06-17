package com.wanted.backend.domain.notice.application.policy;

import com.wanted.backend.domain.notice.application.port.AdminValidationPort;
import com.wanted.backend.domain.notice.application.port.CourseInstructorPort;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class NoticeUpdatePolicy {

    private final CourseInstructorPort courseInstructorPort;
    private final AdminValidationPort adminValidationPort;

    public NoticeUpdatePolicy(CourseInstructorPort courseInstructorPort,
                              AdminValidationPort adminValidationPort) {
        this.courseInstructorPort = courseInstructorPort;
        this.adminValidationPort = adminValidationPort;
    }

    public void validate(Long memberId, Notice notice) {

        if ("COURSE".equals(notice.getType())) {

            if (adminValidationPort.isAdmin(memberId)) {
                return;
            }

            // [강의 공지] 해당 강의 담당 강사인지 확인
            Long instructorId = courseInstructorPort
                    .getInstructorIdByCourseId(notice.getCourseId());

            if (!instructorId.equals(memberId)) {
                throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
            }

        } else {

            // [전체 공지] 관리자인지 확인
            if (!adminValidationPort.isAdmin(memberId)) {
                throw new BusinessException(ErrorCode.NOTICE_ADMIN_ONLY);
            }
        }
    }
}