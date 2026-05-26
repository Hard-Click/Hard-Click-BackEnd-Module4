package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.AccountLockUseCase;
import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockPasswordChangeRequest;
import com.wanted.backend.domain.identity.presentation.api.request.AccountLockVerifyRequest;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountLockService implements AccountLockUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String verify(AccountLockVerifyRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        return verifyEmailUseCase.verifyCode(
                request.getEmail(),
                request.getCode(),
                EmailPurpose.ACCOUNT_LOCK
        );
    }

    @Override
    @Transactional
    public void changePassword(AccountLockPasswordChangeRequest request) {
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        EmailVerification verification = verificationRepository
                .findByVerificationTokenAndPurpose(request.getPasswordChangeToken(), EmailPurpose.ACCOUNT_LOCK)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!verification.isVerified() || verification.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }

        Member member = memberRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        member.changePasswordAndUnlock(
                passwordEncoder.encode(request.getNewPassword()),
                LocalDateTime.now()
        );

        verification.useToken();

        memberRepository.save(member);
        verificationRepository.save(verification);
    }
}