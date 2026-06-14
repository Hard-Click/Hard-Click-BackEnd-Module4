package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.AdminMemberListResult;

import java.util.List;

public record AdminMemberListResponse(
        List<AdminMemberItemResponse> content,
        int page,
        int size,
        boolean hasNext
) {
    public static AdminMemberListResponse from(AdminMemberListResult result) {
        return new AdminMemberListResponse(
                result.items().stream()
                        .map(AdminMemberItemResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.hasNext()
        );
    }
}