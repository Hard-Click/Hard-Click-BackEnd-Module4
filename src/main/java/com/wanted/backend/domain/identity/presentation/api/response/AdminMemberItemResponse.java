package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;

import java.time.LocalDateTime;

public record AdminMemberItemResponse(
        Long memberId,
        String username,
        String name,
        String email,
        String role,
        String status,
        int reportCount,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public static AdminMemberItemResponse from(AdminMemberListResult.Item item) {
        return new AdminMemberItemResponse(
                item.memberId(),
                item.username(),
                item.name(),
                item.email(),
                item.role().name(),
                item.status().name(),
                item.reportCount(),
                item.createdAt(),
                item.lastLoginAt()
        );
    }
}