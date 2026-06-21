package com.wanted.backend.domain.notification.infrastructure.query;

import com.wanted.backend.domain.notification.application.port.MemberIdQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberIdQueryAdapter implements MemberIdQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> findAllAdminIds() {
        return jdbcTemplate.queryForList(
                "SELECT member_id FROM members WHERE role = 'ADMIN'", Long.class);
    }

    @Override
    public List<Long> findAllInstructorIds() {
        return jdbcTemplate.queryForList(
                "SELECT member_id FROM members WHERE role = 'INSTRUCTOR'", Long.class);
    }

    @Override
    public List<Long> findAllStudentIds() {
        return jdbcTemplate.queryForList(
                "SELECT member_id FROM members WHERE role = 'STUDENT'", Long.class);
    }
}