package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.domain.model.Payment;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.repository.PaymentRepository;
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
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository repository;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity(
                payment.getMemberId(),
                payment.getCourseId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getIdempotencyKey()
        );
        try {
            return toDomain(repository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long paymentId) {
        return repository.findById(paymentId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    @Transactional
    public Payment confirmPayment(Long paymentId, String pgTransactionId, LocalDateTime paidAt) {
        PaymentJpaEntity entity = repository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }
        entity.confirm(pgTransactionId, paidAt);
        return toDomain(entity);
    }

    @Override
    @Transactional
    public Payment failPayment(Long paymentId) {
        PaymentJpaEntity entity = repository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }
        entity.markFailed();
        return toDomain(entity);
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstruct(
                entity.getId(),
                entity.getMemberId(),
                entity.getCourseId(),
                entity.getPaidAmount(),
                PaymentStatus.from(entity.getStatus()),
                entity.getIdempotencyKey(),
                entity.getPgTransactionId(),
                entity.getPaidAt()
        );
    }
}
