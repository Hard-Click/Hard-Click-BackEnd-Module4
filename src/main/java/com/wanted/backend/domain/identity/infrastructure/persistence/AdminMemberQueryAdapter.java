package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;
import com.wanted.backend.domain.identity.application.port.AdminMemberQueryPort;
import com.wanted.backend.domain.identity.application.query.AdminMemberListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminMemberQueryAdapter implements AdminMemberQueryPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public AdminMemberListResult findMembers(AdminMemberListQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());

        Slice<MemberJpaEntity> slice = memberJpaRepository.searchAdminMembers(
                normalizeKeyword(query.keyword()),
                query.role(),
                query.status(),
                pageRequest
        );

        return new AdminMemberListResult(
                slice.getContent().stream()
                        .map(this::toItem)
                        .toList(),
                query.page(),
                query.size(),
                slice.hasNext()
        );
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
