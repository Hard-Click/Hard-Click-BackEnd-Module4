package com.wanted.backend.domain.community.application.policy;

import com.wanted.backend.domain.community.application.port.MemberAccessPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommunityAccessPolicy {

    private final MemberAccessPort memberAccessPort;

    public CommunityAccessPolicy(MemberAccessPort memberAccessPort) {
        this.memberAccessPort = memberAccessPort;
    }

    public void validateAccess(Long memberId) {
        if (memberAccessPort.isSuspendedOrWithdrawn(memberId)) {
            throw new BusinessException(ErrorCode.COMMUNITY_ACCESS_DENIED);
        }
    }
}
