package com.wanted.backend.domain.identity.application.dto;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public record AdminMemberListResult(
        List<Item> items,
        int page,
        int size,
        boolean hasNext
) {
    public record Item(
            Long memberId,
            String username,
            String name,
            String email,
            Role role,
            MemberStatus status,
            int reportCount,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt
    ) {
    }
}