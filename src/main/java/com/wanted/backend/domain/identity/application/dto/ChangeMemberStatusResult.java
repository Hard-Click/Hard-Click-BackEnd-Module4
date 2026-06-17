package com.wanted.backend.domain.identity.application.dto;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;

public record ChangeMemberStatusResult(
        Long memberId,
        MemberStatus status,
        String memo
) {
}
