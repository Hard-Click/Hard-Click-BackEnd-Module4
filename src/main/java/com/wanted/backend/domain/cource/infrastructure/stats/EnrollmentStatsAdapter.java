package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.EnrollmentStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentStatsAdapter implements EnrollmentStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public int enrollmentCount(Long courseId) {
        Long count = em.createQuery(
                "SELECT COUNT(e) FROM EnrollmentJpaEntity e WHERE e.courseId = :courseId",
                Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
