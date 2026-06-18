package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.EnrolledCoursePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class EnrolledCourseAdapter implements EnrolledCoursePort {

    private final SpringDataEnrollmentReferenceRepository repository;

    public EnrolledCourseAdapter(SpringDataEnrollmentReferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Long> getEnrolledCourseIdsByMemberId(Long memberId) {
        return repository.findCourseIdsByMemberId(memberId);
    }
}