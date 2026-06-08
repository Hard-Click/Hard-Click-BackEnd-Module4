package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.AccountLockVerifyCommand;
import com.wanted.backend.domain.identity.application.usecase.EmailVerificationUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLockServiceTest {

    @InjectMocks
    private PasswordCommandService passwordCommandService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EmailVerificationRepository verificationRepository;

    @Mock
    private EmailVerificationUseCase emailVerificationUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("잠긴 계정이면 계정 잠금 인증번호를 검증하고 비밀번호 변경 토큰을 발급한다")
    void verify_lockedMember_success() {
        String email = "user@example.com";
        String code = "123456";
        String token = "password-change-token";

        Member member = mock(Member.class);
        when(member.isLocked()).thenReturn(true);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(emailVerificationUseCase.verifyCode(email, code, EmailPurpose.ACCOUNT_LOCK)).thenReturn(token);

        String result = passwordCommandService.verify(new AccountLockVerifyCommand(email, code));

        assertThat(result).isEqualTo(token);
        verify(memberRepository).findByEmail(email);
        verify(emailVerificationUseCase).verifyCode(email, code, EmailPurpose.ACCOUNT_LOCK);
    }
}
