package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;

public interface VerifyEmailUseCase {
    // 인증번호를 생성하여 이메일로 발송합니다.

    void sendVerificationCode(String email, EmailPurpose purpose);

    void sendPasswordResetCode(String email);
    String verifyCode(String email, String code, EmailPurpose purpose);
    void sendAccountLockCode(String email);
}