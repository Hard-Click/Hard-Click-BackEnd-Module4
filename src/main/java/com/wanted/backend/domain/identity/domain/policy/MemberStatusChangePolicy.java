package com.wanted.backend.domain.identity.domain.policy;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;

public final class MemberStatusChangePolicy {

    private MemberStatusChangePolicy() {
    }

    public static boolean isCommunityStatusChangeAllowed(MemberStatus status) {
        return status == MemberStatus.ACTIVE || status == MemberStatus.SUSPENDED;
    }
}
