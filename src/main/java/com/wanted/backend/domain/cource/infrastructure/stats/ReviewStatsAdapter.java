package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.ReviewStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ReviewStatsAdapter implements ReviewStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public double avgRating(Long courseId) {
        Double avg = em.createQuery(
                "SELECT AVG(r.rating) FROM ReviewJpaEntity r WHERE r.courseId = :courseId",
                Double.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    public int reviewCount(Long courseId) {
        Long count = em.createQuery(
                "SELECT COUNT(r) FROM ReviewJpaEntity r WHERE r.courseId = :courseId",
                Long.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
