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

    // 조회용: 비로그인(null, -1)은 통과시키고 로그인 회원이 정지/탈퇴 상태면 차단한다.
    public void validateAccessIfLoggedIn(Long memberId) {
        if (memberId == null || memberId.equals(-1L)) {
            return;
        }
        validateAccess(memberId);
    }
}
