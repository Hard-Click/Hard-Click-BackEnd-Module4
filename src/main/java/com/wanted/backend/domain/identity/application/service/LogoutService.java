package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.LogoutUseCase;
import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository; // (토큰 저장소)

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // 1. 전달받은 Refresh Token이 DB에 있는지 확인 (명세서 404 에러 대응)
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 2. 토큰 삭제 (로그아웃 처리)
        refreshTokenRepository.deleteByMemberId(storedToken.getMemberId());
    }
}