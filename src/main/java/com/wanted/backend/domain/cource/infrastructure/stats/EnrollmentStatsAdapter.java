package com.wanted.backend.domain.cource.infrastructure.stats;

import com.wanted.backend.domain.cource.application.port.EnrollmentStatsPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, Integer> countByCourseIds(Collection<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = em.createQuery(
                "SELECT e.courseId AS courseId, COUNT(e) AS cnt "
                        + "FROM EnrollmentJpaEntity e WHERE e.courseId IN :courseIds GROUP BY e.courseId",
                Tuple.class)
                .setParameter("courseIds", courseIds)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                row -> row.get("courseId", Long.class),
                row -> ((Long) row.get("cnt", Long.class)).intValue()
        ));
    }
}
