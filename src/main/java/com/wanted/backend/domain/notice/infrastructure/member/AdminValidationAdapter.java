package com.wanted.backend.domain.notice.infrastructure.member;

import com.wanted.backend.domain.notice.application.port.AdminValidationPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Transactional(readOnly = true)
public class AdminValidationAdapter implements AdminValidationPort {

    private final SpringDataNoticeMemberReferenceRepository memberReferenceRepository;

    public AdminValidationAdapter(
            SpringDataNoticeMemberReferenceRepository memberReferenceRepository) {
        this.memberReferenceRepository = memberReferenceRepository;
    }

    @Override
    public boolean isAdmin(Long memberId) {
        return memberReferenceRepository.findById(memberId)
                .map(member -> "ADMIN".equals(member.getRole()))
                .orElse(false);
    }
}