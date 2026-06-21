package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    void deleteByMemberId(Long memberId);
}
