package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.EmailSendPort;
import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final EmailVerificationRepository verificationRepository; // (저장소)
    private final EmailSendPort emailSendPort;

    @Override
    @Transactional
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        //  도메인 모델 생성 (숫자 6자리, 5분 유효시간)
        EmailVerification verification = EmailVerification.create(email, purpose);

        //  저장 인프라 계층의 Adapter
        verificationRepository.save(verification);


        //  저장 인프라 계층의 Adapter
        emailSendPort.sendVerificationCode(email, verification.getCode());
    }

    @Override
    @Transactional
    public String verifyCode(String email, String code, EmailPurpose purpose) {
        EmailVerification verification = verificationRepository.findLatestByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND)); // (404 에러)

        try {
            verification.verify(code);
        } catch (RuntimeException e) {
            // [정책 준수] 만료된 경우 410 Gone 에러를 던지도록 처리
            if (e.getMessage().contains("만료")) {
                throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED); // (410 에러)
            }
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH); // (400 에러)
        }

        verificationRepository.save(verification);
        return verification.getVerificationToken();
    }
}