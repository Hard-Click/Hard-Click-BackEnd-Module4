package com.wanted.backend.domain.identity.application.query;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;

public record AdminMemberListQuery(
        String keyword,
        Role role,
        MemberStatus status,
        int page,
        int size
) {
}