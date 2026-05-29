package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLockServiceTest {

    @InjectMocks
    private AccountLockService accountLockService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EmailVerificationRepository verificationRepository;

    @Mock
    private VerifyEmailUseCase verifyEmailUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("잠긴 계정이면 계정 잠금 인증번호를 발송한다")
    void sendCode_lockedMember_success() {
        String email = "user@example.com";

        Member member = mock(Member.class);
        when(member.isLocked()).thenReturn(true);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        accountLockService.sendCode(email);

        verify(memberRepository).findByEmail(email);
        verify(verifyEmailUseCase).sendAccountLockCode(email);
    }
}