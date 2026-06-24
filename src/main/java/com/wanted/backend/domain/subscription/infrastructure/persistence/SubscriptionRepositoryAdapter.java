package com.wanted.backend.domain.subscription.infrastructure.persistence;

import com.wanted.backend.domain.subscription.domain.model.Subscription;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionStatus;
import com.wanted.backend.domain.subscription.domain.repository.SubscriptionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository repository;

    @Override
    @Transactional
    public Subscription save(Subscription subscription) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity(
                subscription.getMemberId(),
                subscription.getPlanId(),
                subscription.getPaymentMethod(),
                subscription.getPaidAmount(),
                subscription.getStatus().name(),
                subscription.getStartedAt(),
                subscription.getExpiredAt(),
                subscription.getCreatedAt()
        );
        try {
            // active_member_id 유니크 제약을 즉시 검증하기 위해 flush까지 강제
            return toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            // 동시 구독 요청으로 활성 구독 중복 생성이 차단된 경우
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findActiveByMemberId(Long memberId) {
        return repository.findFirstByMemberIdAndStatusOrderByStartedAtDesc(memberId, SubscriptionStatus.ACTIVE.name())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findLatestByMemberId(Long memberId) {
        return repository.findFirstByMemberIdOrderByStartedAtDesc(memberId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public Subscription cancelActiveByMemberId(Long memberId, LocalDateTime cancelledAt) {
        SubscriptionJpaEntity entity = repository
                .findFirstByMemberIdAndStatusOrderByStartedAtDesc(memberId, SubscriptionStatus.ACTIVE.name())
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        entity.cancel(cancelledAt);
        return toDomain(entity);
    }

    private Subscription toDomain(SubscriptionJpaEntity entity) {
        return Subscription.restore(
                entity.getId(),
                entity.getMemberId(),
                entity.getPlanId(),
                entity.getPaymentMethod(),
                entity.getPaidAmount(),
                SubscriptionStatus.valueOf(entity.getStatus()),
                entity.getStartedAt(),
                entity.getExpiredAt(),
                entity.getCancelledAt(),
                entity.getCreatedAt()
        );
    }
}
