package com.wanted.backend.domain.community.application.port;

public interface MemberAccessPort {
    boolean isSuspendedOrWithdrawn(Long memberId);
}
