package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.InstructorCoursePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class InstructorCourseAdapter implements InstructorCoursePort {

    private final SpringDataCourseReferenceRepository repository;

    public InstructorCourseAdapter(SpringDataCourseReferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Long> getCourseIdsByInstructorId(Long memberId) {
        return repository.findCourseIdsByAuthorId(memberId);
    }
}