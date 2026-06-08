package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;

public interface EmailVerificationUseCase {
    void sendVerificationCode(String email, EmailPurpose purpose);
    void sendPasswordResetCode(String email);
    String verifyCode(String email, String code, EmailPurpose purpose);
    void sendAccountLockCode(String email);
}
