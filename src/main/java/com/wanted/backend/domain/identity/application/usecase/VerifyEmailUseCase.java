package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;

public interface VerifyEmailUseCase {
    // 인증번호를 생성하여 이메일로 발송합니다.

    void sendVerificationCode(String email, EmailPurpose purpose);


//    입력된 인증번호를 검증합니다.
//    @return 검증 성공 시 발급되는 임시 토큰

    String verifyCode(String email, String code, EmailPurpose purpose);
}