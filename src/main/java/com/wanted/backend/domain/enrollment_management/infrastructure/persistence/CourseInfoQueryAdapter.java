package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.application.port.CourseInfoQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseInfoQueryAdapter implements CourseInfoQueryPort {

    private final EntityManager em;

    @Override
    public String getCourseTitle(Long courseId) {
        return (String) em.createNativeQuery("SELECT title FROM courses WHERE id = :courseId")
                .setParameter("courseId", courseId)
                .getSingleResult();
    }
}
