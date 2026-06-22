package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.port.AdminMemberQueryPort;
import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminMemberQueryAdapter implements AdminMemberQueryPort {

    private final EntityManager entityManager;

    @Override
    public AdminMemberListResult findMembers(AdminMemberListQuery query) {
        List<MemberJpaEntity> members = findMemberSlice(
                normalizeKeyword(query.keyword()),
                query.role(),
                query.status(),
                query.page(),
                query.size()
        );

        boolean hasNext = members.size() > query.size();
        List<MemberJpaEntity> content = hasNext ? members.subList(0, query.size()) : members;
        Map<Long, Long> reportCounts = findReportCounts(content);

        return new AdminMemberListResult(
                content.stream()
                        .map(member -> toItem(member, reportCounts))
                        .toList(),
                query.page(),
                query.size(),
                hasNext
        );
    }

    private List<MemberJpaEntity> findMemberSlice(
            String keyword,
            Role role,
            MemberStatus status,
            int page,
            int size
    ) {
        TypedQuery<MemberJpaEntity> query = entityManager.createQuery("""
                select m
                from MemberJpaEntity m
                where (:keyword is null
                    or lower(m.name) like lower(concat('%', :keyword, '%'))
                    or lower(m.username) like lower(concat('%', :keyword, '%'))
                    or lower(m.email) like lower(concat('%', :keyword, '%')))
                  and (:role is null or m.role = :role)
                  and (:status is null or m.status = :status)
                order by m.createdAt desc, m.id desc
                """, MemberJpaEntity.class);

        query.setParameter("keyword", keyword);
        query.setParameter("role", role);
        query.setParameter("status", status);
        query.setFirstResult(page * size);
        query.setMaxResults(size + 1);

        return query.getResultList();
    }

    private Map<Long, Long> findReportCounts(List<MemberJpaEntity> members) {
        List<Long> memberIds = members.stream()
                .map(MemberJpaEntity::getId)
                .toList();
        if (memberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> reportCounts = new HashMap<>();
        mergeReportCounts(reportCounts, countReports(memberIds));
        return reportCounts;
    }

    private List<Object[]> countReports(List<Long> memberIds) {
        return entityManager.createQuery("""
                select r.reportedMemberId, count(r.id)
                from ReportJpaEntity r
                where r.reportedMemberId in :memberIds
                group by r.reportedMemberId
                """, Object[].class)
                .setParameter("memberIds", memberIds)
                .getResultList();
    }

    private void mergeReportCounts(Map<Long, Long> reportCounts, List<Object[]> rows) {
        for (Object[] row : rows) {
            Long memberId = (Long) row[0];
            Long count = (Long) row[1];
            reportCounts.merge(memberId, count, Long::sum);
        }
    }

    private AdminMemberListResult.Item toItem(MemberJpaEntity member, Map<Long, Long> reportCounts) {
        return new AdminMemberListResult.Item(
                member.getId(),
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getStatus(),
                Math.toIntExact(reportCounts.getOrDefault(member.getId(), 0L)),
                member.getCreatedAt(),
                member.getLastLoginAt()
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
