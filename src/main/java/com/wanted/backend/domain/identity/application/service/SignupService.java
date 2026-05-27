package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.SignupCommand;
import com.wanted.backend.domain.identity.application.usecase.SignupUseCase;
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

@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long signup(SignupCommand command) {
        if (!command.password().equals(command.passwordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        EmailVerification verification = verificationRepository
                .findLatestByEmailAndPurpose(command.email(), EmailPurpose.SIGNUP)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isVerified()
                || !verification.getVerificationToken().equals(command.emailVerificationToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (memberRepository.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (memberRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.create(
                command.username(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.gender(),
                command.birthDate(),
                command.phoneNumber(),
                command.profileImageUrl(),
                Role.STUDENT
        );

        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }
}