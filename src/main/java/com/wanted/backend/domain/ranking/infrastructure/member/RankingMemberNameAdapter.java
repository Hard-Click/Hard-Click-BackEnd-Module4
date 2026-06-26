package com.wanted.backend.domain.ranking.infrastructure.member;

import com.wanted.backend.domain.community.infrastructure.member.MemberReferenceEntity;
import com.wanted.backend.domain.community.infrastructure.member.SpringDataMemberReferenceRepository;
import com.wanted.backend.domain.ranking.application.port.MemberNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RankingMemberNameAdapter implements MemberNamePort {

    private final SpringDataMemberReferenceRepository memberReferenceRepository;

    @Override
    public Map<Long, String> getNamesByMemberIds(Collection<Long> memberIds) {
        return memberReferenceRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(MemberReferenceEntity::getId, MemberReferenceEntity::getName));
    }
}
