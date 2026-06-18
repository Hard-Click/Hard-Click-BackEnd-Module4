package com.wanted.backend.domain.identity.application.dto;

import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;

import java.time.LocalDateTime;

public record MemberStatusSyncMessage(
        Long memberId,
        MemberStatus status,
        String message,
        LocalDateTime occurredAt
) {
    public static MemberStatusSyncMessage from(Member member, LocalDateTime occurredAt) {
        return new MemberStatusSyncMessage(
                member.getId(),
                member.getStatus(),
                statusMessage(member.getStatus()),
                occurredAt
        );
    }

    private static String statusMessage(MemberStatus status) {
        if (status == MemberStatus.SUSPENDED) {
            return "커뮤니티 작성이 제한된 상태입니다.";
        }
        return null;
    }
}
