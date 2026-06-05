package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.VerifyEmailUseCase;
import com.wanted.backend.domain.identity.application.usecase.LoginUseCase;
import com.wanted.backend.domain.identity.domain.model.AuthToken;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final VerifyEmailUseCase verifyEmailUseCase;

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthToken login(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_INFO));

        if (member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        boolean passwordMatched =
                passwordEncoder.matches(rawPassword, member.getPassword());

        if (!passwordMatched) {
            member.loginFailed(LocalDateTime.now());
            memberRepository.save(member);

            if (member.isLocked()) {
                verifyEmailUseCase.sendAccountLockCode(member.getEmail());
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }

            throw new BusinessException(ErrorCode.INVALID_LOGIN_INFO);
        }

        member.loginSuccess(LocalDateTime.now());
        memberRepository.save(member);
        member.pullDomainEvents().forEach(eventPublisher::publishEvent);

        String role = "ROLE_" + member.getRole().name();
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getUsername(), role);
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenRepository.deleteByMemberId(member.getId());
        saveRefreshToken(member.getId(), refreshToken);

        return new AuthToken(accessToken, refreshToken, member.getId(), role);
    }

    @Override
    @Transactional
    public AuthToken refresh(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken) ||
                !"refresh".equals(jwtProvider.getTokenType(refreshToken))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (storedToken.isExpired()) {
            refreshTokenRepository.deleteByMemberId(storedToken.getMemberId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long memberId = storedToken.getMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String role = "ROLE_" + member.getRole().name();
        String newAccessToken = jwtProvider.createAccessToken(memberId, member.getUsername(), role);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);

        refreshTokenRepository.deleteByMemberId(memberId);
        saveRefreshToken(memberId, newRefreshToken);

        return new AuthToken(newAccessToken, newRefreshToken, memberId, role);
    }

    private void saveRefreshToken(Long memberId, String token) {
        RefreshToken refreshTokenModel = new RefreshToken(
                null,
                memberId,
                token,
                LocalDateTime.now().plusDays(14),
                LocalDateTime.now()
        );
        refreshTokenRepository.save(refreshTokenModel);
    }
}