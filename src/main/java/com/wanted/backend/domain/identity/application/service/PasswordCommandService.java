package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.AccountLockPasswordChangeCommand;
import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.command.ResetPasswordCommand;
import com.wanted.backend.domain.identity.application.command.UpdatePasswordCommand;
import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.application.usecase.PasswordCommandUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordCommandService implements PasswordCommandUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailVerificationTokenCoordinator verificationTokenCoordinator;
    private final EmailVerificationUseCase emailVerificationUseCase;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updatePassword(Long memberId, UpdatePasswordCommand command) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!command.newPassword().equals(command.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        member.changePassword(passwordEncoder.encode(command.newPassword()));
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        if (!command.newPassword().equals(command.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        verificationTokenCoordinator.reserveForCurrentTransaction(
                command.email(),
                command.passwordChangeToken(),
                EmailPurpose.PASSWORD_RESET
        );
        member.changePasswordAndUnlock(passwordEncoder.encode(command.newPassword()), LocalDateTime.now());

        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyCurrentPassword(Long memberId, String currentPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    @Override
    @Transactional
    public String verify(AccountLockVerifyCommand command) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        return emailVerificationUseCase.verifyCode(
                command.email(),
                command.code(),
                EmailPurpose.ACCOUNT_LOCK
        );
    }

    @Override
    @Transactional
    public void changePassword(AccountLockPasswordChangeCommand command) {
        if (!command.newPassword().equals(command.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        EmailVerification verification = verificationRepository
                .findByVerificationTokenAndPurpose(command.passwordChangeToken(), EmailPurpose.ACCOUNT_LOCK)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Member member = memberRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        verificationTokenCoordinator.reserveForCurrentTransaction(
                verification.getEmail(),
                command.passwordChangeToken(),
                EmailPurpose.ACCOUNT_LOCK
        );

        member.changePasswordAndUnlock(
                passwordEncoder.encode(command.newPassword()),
                LocalDateTime.now()
        );

        memberRepository.save(member);
    }
}
