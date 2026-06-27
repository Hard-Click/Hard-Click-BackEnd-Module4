package com.wanted.backend.domain.subscription.infrastructure.persistence;

import com.wanted.backend.domain.subscription.domain.model.Subscription;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionStatus;
import com.wanted.backend.domain.subscription.domain.repository.SubscriptionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
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
                subscription.getOrderId(),
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
            // 활성 구독 유니크 제약(active_member_id) 위반일 때만 "이미 구독 중"으로 변환한다.
            // 그 외 제약 위반(NOT NULL, 채번 실패 등)은 원인을 가리지 않도록 원래 예외를 전파한다.
            if (isActiveMemberUniqueViolation(e)) {
                throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
            }
            throw e;
        }
    }

    private boolean isActiveMemberUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            String constraintName = cve.getConstraintName();
            return constraintName != null && constraintName.toLowerCase().contains("active_member");
        }
        return false;
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
                entity.getOrderId(),
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
