package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.EmailSendPort;
import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.policy.EmailPolicy;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService implements EmailVerificationUseCase {

    private final EmailVerificationRepository verificationRepository;
    private final EmailSendPort emailSendPort;
    private final MemberRepository memberRepository;

    @Value("${identity.email.allowed-domain:gmail.com}")
    private String allowedEmailDomain;

    @Override
    @Transactional
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        if (!EmailPolicy.isAllowedDomain(email, allowedEmailDomain)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }
        verificationRepository.revokeActiveByEmailAndPurpose(email, purpose);

        EmailVerification verification = EmailVerification.create(email, purpose);
        verificationRepository.save(verification);
        emailSendPort.sendVerificationCode(email, verification.getCode());
    }

    @Override
    @Transactional
    public void sendPasswordResetCode(String email) {
        if (!memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayCount = verificationRepository.countByEmailAndPurposeAndCreatedAtAfter(
                email, EmailPurpose.PASSWORD_RESET, startOfToday
        );

        if (todayCount >= 3) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_LIMIT_EXCEEDED);
        }

        sendVerificationCode(email, EmailPurpose.PASSWORD_RESET);
    }

    @Override
    @Transactional
    public String verifyCode(String email, String code, EmailPurpose purpose) {
        EmailVerification verification = verificationRepository.findLatestPendingByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        try {
            verification.verify(code);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("만료")) {
                throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
            }
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        verificationRepository.save(verification);
        return verification.getVerificationToken();
    }

    @Override
    @Transactional
    public void sendAccountLockCode(String email) {
        sendVerificationCode(email, EmailPurpose.ACCOUNT_LOCK);
    }
}
