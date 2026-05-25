package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.UpdatePasswordUseCase;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.presentation.api.request.UpdatePasswordRequest;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePasswordService implements UpdatePasswordUseCase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updatePassword(Long memberId, UpdatePasswordRequest request) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD); // (현재 비번 틀림)
        }

        // 3. 새 비밀번호와 확인 값 일치 여부 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 4. 비밀번호 변경 및 저장
        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }
}