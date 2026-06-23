package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.InstructorStatsPort;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    @SuppressWarnings("unchecked")
    public CourseCounts courseCounts(Long authorId) {
        List<Object[]> rows = em.createQuery(
                "SELECT c.status, COUNT(c) FROM CourseJpaEntity c " +
                "WHERE c.authorId = :authorId AND c.status != :deleted GROUP BY c.status")
                .setParameter("authorId", authorId)
                .setParameter("deleted", CourseStatus.DELETED)
                .getResultList();

        int published = 0;
        int hidden = 0;
        for (Object[] row : rows) {
            CourseStatus status = (CourseStatus) row[0];
            int count = ((Number) row[1]).intValue();
            if (status == CourseStatus.PUBLISHED) {
                published = count;
            } else if (status == CourseStatus.DRAFT) {
                hidden = count;
            }
        }
        return new CourseCounts(published + hidden, published, hidden);
    }
}
