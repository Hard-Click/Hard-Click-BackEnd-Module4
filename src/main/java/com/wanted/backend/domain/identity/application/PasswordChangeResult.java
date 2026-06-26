package com.wanted.backend.domain.identity.application;

import com.wanted.backend.domain.identity.domain.model.Member;

public record PasswordChangeResult(Long memberId) {
    public static PasswordChangeResult from(Member member) {
        return new PasswordChangeResult(member.getId());
    }
}
