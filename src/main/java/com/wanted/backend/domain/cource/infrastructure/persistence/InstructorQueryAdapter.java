package com.wanted.backend.domain.cource.infrastructure.persistence;

import com.wanted.backend.domain.cource.application.port.InstructorQueryPort;
import com.wanted.backend.domain.cource.application.port.InstructorQueryPort.InstructorProfile;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * identity 도메인의 Member 엔티티를 직접 참조하지 않고
 * native SQL로 members 테이블만 조회 (크로스 컨텍스트 규칙 준수)
 */
@Component
@RequiredArgsConstructor
public class InstructorQueryAdapter implements InstructorQueryPort {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, String> findNamesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        List<Object[]> rows = em.createNativeQuery(
                        "SELECT member_id, name FROM members WHERE member_id IN (:ids)")
                .setParameter("ids", ids)
                .getResultList();
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r[0]).longValue(),
                r -> (String) r[1]
        ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> findIdsByName(String name) {
        List<Object> rows = em.createNativeQuery(
                        "SELECT member_id FROM members WHERE name LIKE :name")
                .setParameter("name", "%" + name + "%")
                .getResultList();
        return rows.stream()
                .map(r -> ((Number) r).longValue())
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public InstructorProfile findProfileById(Long instructorId) {
        List<Object[]> rows = em.createNativeQuery(
                        "SELECT one_line_intro, introduction, career FROM members WHERE member_id = :id")
                .setParameter("id", instructorId)
                .getResultList();
        if (rows.isEmpty()) {
            return InstructorProfile.empty();
        }
        Object[] row = rows.get(0);
        return new InstructorProfile((String) row[0], (String) row[1], (String) row[2]);
    }
}
