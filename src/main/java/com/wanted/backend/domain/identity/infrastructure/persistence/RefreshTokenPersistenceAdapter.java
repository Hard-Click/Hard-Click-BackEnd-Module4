package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.RefreshToken;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByMemberId(Long memberId) {
        return refreshTokenJpaRepository.findByMemberId(memberId)
                .map(this::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        // 도메인 모델을 JPA 엔티티로 변환
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
                refreshToken.getToken(),
                refreshToken.getMemberId(),
                refreshToken.getExpiryDate(),
                refreshToken.getCreatedAt()
        );

        RefreshTokenJpaEntity savedEntity = refreshTokenJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        refreshTokenJpaRepository.deleteByMemberId(memberId);
    }

    // JPA 엔티티를 도메인 모델로 변환 (Mapper)
    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getMemberId(),
                entity.getToken(),
                entity.getExpiryDate(),
                entity.getCreatedAt()
        );
    }
}