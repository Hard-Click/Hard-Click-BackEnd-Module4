package com.wanted.backend.domain.community.infrastructure.member;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Transactional(readOnly = true)
public class MemberNameAdapter implements MemberNamePort {

    private final SpringDataMemberReferenceRepository memberReferenceRepository;

    public MemberNameAdapter(SpringDataMemberReferenceRepository memberReferenceRepository) {
        this.memberReferenceRepository = memberReferenceRepository;
    }

    @Override
    public String getNameByMemberId(Long memberId) {
        return memberReferenceRepository.findById(memberId)
                .map(MemberReferenceEntity::getName)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Map<Long, String> getNamesByMemberIds(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) return Map.of();
        Map<Long, String> result = new HashMap<>();
        memberReferenceRepository.findByIdIn(memberIds)
                .forEach(e -> result.put(e.getId(), e.getName()));
        return result;
    }
}