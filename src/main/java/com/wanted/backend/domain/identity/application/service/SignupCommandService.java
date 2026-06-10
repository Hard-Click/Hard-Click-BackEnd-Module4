package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.SignupCommand;
import com.wanted.backend.domain.identity.application.usecase.SignupCommandUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.Role;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignupCommandService implements SignupCommandUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long signup(SignupCommand command) {
        EmailVerification verification = verificationRepository
                .findValidToken(
                        command.email(),
                        command.emailVerificationToken(),
                        EmailPurpose.SIGNUP,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (memberRepository.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (memberRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        verification.useToken();
        verificationRepository.save(verification);

        Member member = Member.create(
                command.username(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.gender(),
                command.birthDate(),
                command.phoneNumber(),
                command.profileImageUrl(),
                Role.STUDENT,
                Boolean.TRUE.equals(command.optionalTermsAgreed())
        );

        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameDuplicated(String username) {
        return memberRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }
}