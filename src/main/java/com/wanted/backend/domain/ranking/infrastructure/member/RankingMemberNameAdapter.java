package com.wanted.backend.domain.ranking.infrastructure.member;

import com.wanted.backend.domain.ranking.application.port.MemberNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RankingMemberNameAdapter implements MemberNamePort {

    private final com.wanted.backend.domain.community.application.port.MemberNamePort communityMemberNamePort;

    @Override
    public Map<Long, String> getNamesByMemberIds(Collection<Long> memberIds) {
        return communityMemberNamePort.getNamesByMemberIds(memberIds);
    }
}
