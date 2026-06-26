package com.wanted.backend.domain.community.application.port;

import java.util.Collection;
import java.util.Map;

public interface MemberNamePort {
    String getNameByMemberId(Long memberId);

    Map<Long, String> getNamesByMemberIds(Collection<Long> memberIds);
}