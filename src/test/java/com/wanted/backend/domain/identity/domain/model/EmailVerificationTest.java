package com.wanted.backend.domain.identity.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailVerificationTest {

    @Test
    @DisplayName("인증번호가 일치하면 인증 완료 처리되고 인증 토큰이 생성된다")
    void verify_success() {
        EmailVerification verification = EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
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
                EmailPurpose.SIGNUP,
                Duration.ofMinutes(3)
        );

        assertThat(verification.getCode()).matches("\\d{6}");
        assertThat(verification.getCodeHash()).hasSize(64);
        assertThat(verification.getCodeHash()).isNotEqualTo(verification.getCode());
    }

    @Test
    void create_rejectsInvalidCodeTtl() {
        assertThatThrownBy(() -> EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                null
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThatThrownBy(() -> EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ZERO
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThatThrownBy(() -> EmailVerification.create(
                "user@example.com",
                EmailPurpose.SIGNUP,
                Duration.ofSeconds(-1)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }
}
