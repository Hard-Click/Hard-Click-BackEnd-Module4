package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;
import com.wanted.backend.domain.identity.application.usecase.WithdrawMemberUseCase;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawMemberService implements WithdrawMemberUseCase {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void withdraw(Long memberId, WithdrawMemberCommand command) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }

        if (!passwordEncoder.matches(command.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        System.out.println("탈퇴 전 status = " + member.getStatus());
        System.out.println("탈퇴 전 email = " + member.getEmail());
        member.withdraw(LocalDateTime.now());
        System.out.println("탈퇴 후 status = " + member.getStatus());
        System.out.println("탈퇴 후 email = " + member.getEmail());

        memberRepository.save(member);

        refreshTokenRepository.deleteByMemberId(memberId);
    }
}