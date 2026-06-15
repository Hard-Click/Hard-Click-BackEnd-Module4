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

import java.util.List;

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

        return new AdminMemberListResult(
                content.stream()
                        .map(this::toItem)
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

    private AdminMemberListResult.Item toItem(MemberJpaEntity member) {
        return new AdminMemberListResult.Item(
                member.getId(),
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getStatus(),
                0,
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
