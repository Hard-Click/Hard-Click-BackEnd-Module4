package com.wanted.backend.domain.notification.infrastructure.query;

import com.wanted.backend.domain.notification.application.port.CourseEnrolleeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 강좌 수강생 ID 목록 조회 어댑터.
 * EnrollmentRepository에 courseId 기반 조회가 없어서 JdbcTemplate으로 직접 조회한다.
 * (추후 EnrollmentRepository에 메서드 추가 시 대체 가능)
 */
@Component
@RequiredArgsConstructor
public class CourseEnrolleeQueryAdapter implements CourseEnrolleeQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> findMemberIdsByCourseId(Long courseId) {
        return jdbcTemplate.queryForList(
                "SELECT member_id FROM enrollment WHERE course_id = ? AND status != 'WITHDRAWN'",
                Long.class,
                courseId
        );
    }
}