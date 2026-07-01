package com.wanted.backend.domain.community.application.port;

import java.util.Collection;
import java.util.Map;

public interface MemberNamePort {
    String getNameByMemberId(Long memberId);

    // 방법②: Batch IN — N번 개별 조회 → 1번 IN 쿼리
    Map<Long, String> getNamesByMemberIds(Collection<Long> memberIds);
}