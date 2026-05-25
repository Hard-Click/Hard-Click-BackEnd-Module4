package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.SignupUseCase;
import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.Role;
import org.springframework.transaction.annotation.Transactional;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.presentation.api.request.SignupRequest;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long signup(SignupRequest request) { // (명세서에 따라 memberId 반환)
        // 1. 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 2. 이메일 인증 토큰 검증 (UA-P0-055)
        EmailVerification verification = verificationRepository.findLatestByEmailAndPurpose(request.getEmail(), EmailPurpose.SIGNUP)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isVerified() || !verification.getVerificationToken().equals(request.getEmailVerificationToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // (명세서 401 반영)
        }

        // 3. 중복 체크
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 4. 저장
        Member member = Member.create(
                request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(), request.getGender(), request.getBirthDate(),
                request.getPhoneNumber(), request.getProfileImageUrl(), Role.STUDENT
        );

        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }
}