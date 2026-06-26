package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.InstructorCoursePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class InstructorCourseAdapter implements InstructorCoursePort {

    private final SpringDataCourseReferenceRepository courseReferenceRepository;

    public InstructorCourseAdapter(SpringDataCourseReferenceRepository courseReferenceRepository) {
        this.courseReferenceRepository = courseReferenceRepository;
    }

    // 강사 ID로 담당 강의 ID 목록 조회
    @Override
    public List<Long> getCourseIdsByInstructorId(Long memberId) {
        return courseReferenceRepository.findByAuthorId(memberId)
                .stream()
                .map(CourseReferenceEntity::getId)
                .toList();
    }
}