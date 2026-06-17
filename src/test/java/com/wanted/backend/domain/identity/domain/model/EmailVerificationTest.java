package com.wanted.backend.domain.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTest {

    @Test
    @DisplayName("인증번호가 일치하면 인증 완료 처리되고 인증 토큰이 생성된다")
    void verify_success() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP
        );

        verification.verify(verification.getCode());

        assertThat(verification.isVerified()).isTrue();
        assertThat(verification.getVerifiedAt()).isNotNull();
        assertThat(verification.getVerificationToken()).isNotBlank();
    }

    @Test
    void create_generatesSixDigitNumericCode() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP
        );

        assertThat(verification.getCode()).matches("\\d{6}");
    }
}
