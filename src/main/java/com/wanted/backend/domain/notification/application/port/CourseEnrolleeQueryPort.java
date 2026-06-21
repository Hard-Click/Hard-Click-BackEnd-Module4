package com.wanted.backend.domain.notification.application.port;

import java.util.List;

/**
 * 강좌 수강생 ID 목록을 조회하는 포트.
 * 강의 공지 알림 발송 시 수신자를 찾기 위해 사용된다.
 */
public interface CourseEnrolleeQueryPort {
    List<Long> findMemberIdsByCourseId(Long courseId);
}