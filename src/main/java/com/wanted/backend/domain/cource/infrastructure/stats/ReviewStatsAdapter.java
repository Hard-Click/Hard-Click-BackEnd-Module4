package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.ReviewStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, Stats> findStatsByCourseIds(Collection<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = em.createQuery(
                "SELECT r.courseId AS courseId, AVG(r.rating) AS avgRating, COUNT(r) AS reviewCount "
                        + "FROM ReviewJpaEntity r WHERE r.courseId IN :courseIds GROUP BY r.courseId",
                Tuple.class)
                .setParameter("courseIds", courseIds)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                row -> row.get("courseId", Long.class),
                row -> {
                    double avg = row.get("avgRating", Double.class);
                    long count = row.get("reviewCount", Long.class);
                    return new Stats(Math.round(avg * 10.0) / 10.0, (int) count);
                }
        ));
    }
}
