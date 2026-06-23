package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.community.application.port.MemberAccessPort;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberAccessAdapter implements MemberAccessPort {

    private final MemberJpaRepository memberJpaRepository;

    public MemberAccessAdapter(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public boolean isSuspendedOrWithdrawn(Long memberId) {
        return memberJpaRepository.existsByIdAndStatusIn(
                memberId,
                List.of(MemberStatus.SUSPENDED, MemberStatus.WITHDRAWN)
        );
    }
}
