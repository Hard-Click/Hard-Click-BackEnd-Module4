package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.application.port.CourseInfoQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseInfoQueryAdapter implements CourseInfoQueryPort {

    private final EntityManager em;

    @Override
    public String getCourseTitle(Long courseId) {
        List<?> results = em.createNativeQuery("SELECT title FROM course WHERE course_id = :courseId")
                .setParameter("courseId", courseId)
                .getResultList();
        return results.isEmpty() ? "(삭제된 강의)" : (String) results.get(0);
    }
}
