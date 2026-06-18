package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.EmailSendPort;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository verificationRepository;
    @Mock
    private EmailSendPort emailSendPort;
    @Mock
    private MemberRepository memberRepository;

    @Test
    void usesConfiguredPasswordResetDailyLimit() {
        int configuredLimit = 7;
        EmailVerificationService service = new EmailVerificationService(
                verificationRepository,
                emailSendPort,
                memberRepository,
                Duration.ofMinutes(5),
                "example.com",
                configuredLimit
        );
        when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(verificationRepository.tryAcquireSendPermission(
                eq("user@example.com"),
                eq(EmailPurpose.PASSWORD_RESET),
                eq(configuredLimit),
                any(LocalDateTime.class)
        )).thenReturn(true);

        service.sendPasswordResetCode("user@example.com");

        verify(verificationRepository).tryAcquireSendPermission(
                eq("user@example.com"),
                eq(EmailPurpose.PASSWORD_RESET),
                eq(configuredLimit),
                any(LocalDateTime.class)
        );
        verify(verificationRepository).save(any());
    }

    @Test
    void rejectsPasswordResetWhenDailyLimitIsExceeded() {
        EmailVerificationService service = createService(3);
        when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(verificationRepository.tryAcquireSendPermission(
                eq("user@example.com"),
                eq(EmailPurpose.PASSWORD_RESET),
                eq(3),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertThatThrownBy(() -> service.sendPasswordResetCode("user@example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_LIMIT_EXCEEDED);

        verify(verificationRepository, never()).save(any());
    }

    @Test
    void rejectsPasswordResetForUnknownEmail() {
        EmailVerificationService service = createService(3);
        when(memberRepository.existsByEmail("missing@example.com")).thenReturn(false);

        assertThatThrownBy(() -> service.sendPasswordResetCode("missing@example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(verificationRepository, never()).tryAcquireSendPermission(
                any(), any(), any(Integer.class), any(LocalDateTime.class)
        );
    }

    private EmailVerificationService createService(int dailyLimit) {
        return new EmailVerificationService(
                verificationRepository,
                emailSendPort,
                memberRepository,
                Duration.ofMinutes(5),
                "example.com",
                dailyLimit
        );
    }
}
