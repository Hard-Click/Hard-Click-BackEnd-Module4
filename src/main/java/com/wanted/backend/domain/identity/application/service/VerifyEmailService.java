package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.EmailSendPort;
import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final EmailVerificationRepository verificationRepository;
    private final EmailSendPort emailSendPort;
    private final MemberRepository memberRepository;

    /**
     * [기본 기능] 인증번호 생성 및 이메일 발송
     */
    @Override
    @Transactional
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        // 도메인 모델 생성 (숫자 6자리, 5분 유효시간)
        EmailVerification verification = EmailVerification.create(email, purpose);

        // 저장 (Persistence Adapter 호출)
        verificationRepository.save(verification);

        // 발송 (Email Send Adapter 호출)
        emailSendPort.sendVerificationCode(email, verification.getCode());
    }

    /**
     * [비밀번호 찾기용] 가입 여부 확인 및 하루 3회 제한 로직 포함 발송
     */
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

        // 3. [검수 3] 모든 검증 통과 시 기존 발송 로직 재사용 (목적: PASSWORD_RESET)
        sendVerificationCode(email, EmailPurpose.PASSWORD_RESET);
    }

    /**
     * [공통 기능] 인증번호 검증 및 토큰 발급
     */
    @Override
    @Transactional
    public String verifyCode(String email, String code, EmailPurpose purpose) {

        EmailVerification verification = verificationRepository.findLatestByEmailAndPurpose(email, purpose)
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
}