package com.wanted.backend.domain.payment.infrastructure.course;

import com.wanted.backend.domain.payment.application.port.CourseForOrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseForOrderAdapter implements CourseForOrderQueryPort {

    private final PaymentCourseReferenceRepository repository;

    @Override
    public List<CourseInfo> findAllByIds(List<Long> courseIds) {
        return repository.findByIdIn(courseIds).stream()
                .map(e -> new CourseInfo(e.getId(), e.getTitle(), e.getPrice()))
                .toList();
    }
}
