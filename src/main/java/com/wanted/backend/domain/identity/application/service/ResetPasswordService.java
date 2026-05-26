package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.ResetPasswordUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.presentation.api.request.ResetPasswordRequest;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. 비밀번호 확인 일치 여부 체크
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2. [중요] 이메일 인증 토큰 검증
        EmailVerification verification = verificationRepository.findLatestByEmailAndPurpose(request.getEmail(), EmailPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isVerified() || !verification.getVerificationToken().equals(request.getPasswordChangeToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // (인증 안 됐거나 토큰 다름)
        }

        // 3. 회원 찾기 및 비밀번호 교체
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }
}