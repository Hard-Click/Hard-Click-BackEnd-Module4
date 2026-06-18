package com.wanted.backend.domain.identity.presentation.api.response;

import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;

public record ChangeMemberStatusResponse(
        Long memberId,
        String status,
        String memo
) {
    public static ChangeMemberStatusResponse from(ChangeMemberStatusResult result) {
        return new ChangeMemberStatusResponse(
                result.memberId(),
                result.status().name(),
                result.memo()
        );
    }
}
