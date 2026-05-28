package com.wanted.backend.domain.notice.application.policy;

import com.wanted.backend.domain.notice.application.port.CourseInstructorPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;


@Component
public class NoticeCreatePolicy {

    private final CourseInstructorPort courseInstructorPort;

    public NoticeCreatePolicy(CourseInstructorPort courseInstructorPort) {
        this.courseInstructorPort = courseInstructorPort;
    }

    public void validate(Long instructorId, Long courseId) {

        // [1단계] 강의 담당 강사 ID 조회 (존재하지 않는 강의면 예외 발생)
        Long courseInstructorId = courseInstructorPort.getInstructorIdByCourseId(courseId);

        // [2단계] 해당 강의의 담당 강사인지 확인
        if (!courseInstructorId.equals(instructorId)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
        }
    }
}