package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.InstructorStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class InstructorStatsAdapter implements InstructorStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public int totalStudents(Long authorId) {
        Long count = em.createQuery(
                "SELECT COUNT(e) FROM EnrollmentJpaEntity e " +
                "WHERE e.courseId IN (SELECT c.id FROM CourseJpaEntity c WHERE c.authorId = :authorId)",
                Long.class)
                .setParameter("authorId", authorId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalCourses(Long authorId) {
        Long count = em.createQuery(
                "SELECT COUNT(c) FROM CourseJpaEntity c " +
                "WHERE c.authorId = :authorId AND c.status = 'PUBLISHED'",
                Long.class)
                .setParameter("authorId", authorId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public double avgRating(Long authorId) {
        Double avg = em.createQuery(
                "SELECT AVG(r.rating) FROM ReviewJpaEntity r " +
                "WHERE r.courseId IN (SELECT c.id FROM CourseJpaEntity c WHERE c.authorId = :authorId)",
                Double.class)
                .setParameter("authorId", authorId)
                .getSingleResult();
        if (avg == null) return 0.0;
        return Math.round(avg * 10.0) / 10.0;
    }
}
