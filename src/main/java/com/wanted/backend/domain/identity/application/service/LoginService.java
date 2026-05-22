package com.wanted.backend.domain.identity.application.service;

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
public class LoginService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    // Spring의 이벤트 발행자 주입
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthToken login(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_INFO));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_INFO);
        }

        // [Step 1] 도메인 행위 수행 (내부에서 이벤트가 Record됨)
        member.loginSuccess(LocalDateTime.now());

        // [Step 2] 변경된 도메인 상태 저장
        memberRepository.save(member);

        // [Step 3] 도메인에서 이벤트를 꺼내어(Pull) 스프링 이벤트로 발행(Publish)
        member.pullDomainEvents().forEach(eventPublisher::publishEvent);

        // 토큰 발급 로직...
        String role = "ROLE_" + member.getRole().name();
        String accessToken = jwtProvider.createAccessToken(member.getId(), role);
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenRepository.deleteByMemberId(member.getId());
        saveRefreshToken(member.getId(), refreshToken);

        return new AuthToken(accessToken, refreshToken);
    }

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
        String newAccessToken = jwtProvider.createAccessToken(memberId, role);
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);

        refreshTokenRepository.deleteByMemberId(memberId);
        saveRefreshToken(memberId, newRefreshToken);

        return new AuthToken(newAccessToken, newRefreshToken);
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