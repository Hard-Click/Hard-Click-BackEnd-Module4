package com.wanted.backend.domain.community.application.port;

public interface MemberNamePort {
    String getNameByMemberId(Long memberId);
}