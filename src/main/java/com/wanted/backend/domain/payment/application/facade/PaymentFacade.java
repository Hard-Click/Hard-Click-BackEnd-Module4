package com.wanted.backend.domain.payment.application.facade;

import com.wanted.backend.domain.payment.application.port.PgClient;
import com.wanted.backend.domain.payment.domain.model.Payment;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.repository.PaymentRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 동일 멱등키로 동시에 들어온 결제 요청이 단 한 번만 처리되도록
 * Redis 분산락 + 멱등키 캐시를 적용한 결제 확정 흐름.
 *
 * 락:  payment:lock:{idempotencyKey} (TTL 30초, SETNX)
 * 멱등: payment:idem:{idempotencyKey} (TTL 10분, paymentId 저장)
 */
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private static final String LOCK_KEY_PREFIX = "payment:lock:";
    private static final String IDEMPOTENCY_KEY_PREFIX = "payment:idem:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;
    private final MeterRegistry meterRegistry;

    public Result confirm(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "FAILED";
        try {
            Result result = doConfirm(memberId, courseId, amount, idempotencyKey);
            outcome = result.duplicate() ? "DUPLICATE" : "SUCCESS";
            return result;
        } catch (BusinessException e) {
            outcome = e.getErrorCode() == ErrorCode.DUPLICATE_PAYMENT_REQUEST ? "DUPLICATE" : "FAILED";
            throw e;
        } finally {
            recordResult(outcome);
            sample.stop(Timer.builder("payment.processing.duration").register(meterRegistry));
        }
    }

    private Result doConfirm(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        // 1. Redis 멱등키 캐시 먼저 확인 — 이미 처리 완료된 요청이면 DB 조회 없이 즉시 반환
        String cachedPaymentId = redisTemplate.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + idempotencyKey);
        if (cachedPaymentId != null) {
            Payment cached = paymentRepository.findById(Long.parseLong(cachedPaymentId)).orElse(null);
            if (cached != null) {
                validateOwner(cached, memberId);
                return Result.from(cached, true);
            }
        }

        // 2. DB 확인 — 이미 처리된(혹은 처리 중인) 멱등키인지 확인
        Payment existing = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            validateOwner(existing, memberId);
            return Result.from(existing, true);
        }

        String lockKey = LOCK_KEY_PREFIX + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);

        if (acquired == null || !acquired) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }

        try {
            // 락 획득 후 재확인 (락 대기 중 다른 스레드가 이미 처리를 끝냈을 수 있음)
            Payment racedExisting = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (racedExisting != null) {
                validateOwner(racedExisting, memberId);
                return Result.from(racedExisting, true);
            }

            return processPayment(memberId, courseId, amount, idempotencyKey);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private void validateOwner(Payment payment, Long memberId) {
        if (!payment.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Result processPayment(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        Payment created = createPending(memberId, courseId, amount, idempotencyKey);

        String pgTransactionId;
        try {
            pgTransactionId = pgClient.confirm(memberId, courseId, amount);
        } catch (RuntimeException e) {
            failPayment(created.getId());
            throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
        }

        Payment confirmed = confirmPayment(created.getId(), pgTransactionId);

        // 멱등키 캐시 저장 (TTL 10분) — 동일 키로 재요청 시 Redis hit으로 즉시 반환
        redisTemplate.opsForValue().set(IDEMPOTENCY_KEY_PREFIX + idempotencyKey, String.valueOf(confirmed.getId()), IDEMPOTENCY_TTL);

        return Result.from(confirmed, false);
    }

    // 아래 세 메서드는 각각 PaymentRepositoryAdapter에서 독립적인 @Transactional로 즉시 커밋된다.
    // PENDING 저장 → (커밋) → PG 호출(트랜잭션 밖) → (커밋) confirm/fail 순서가 되어,
    // PG 호출 동안 DB 커넥션을 점유하지 않는다.
    private Payment createPending(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        return paymentRepository.save(Payment.create(memberId, courseId, amount, idempotencyKey));
    }

    private Payment confirmPayment(Long paymentId, String pgTransactionId) {
        return paymentRepository.confirmPayment(paymentId, pgTransactionId, LocalDateTime.now());
    }

    private void failPayment(Long paymentId) {
        paymentRepository.failPayment(paymentId);
    }

    private void recordResult(String outcome) {
        Counter.builder("payment.result")
                .tag("status", outcome)
                .register(meterRegistry)
                .increment();
    }

    public record Result(Long paymentId, PaymentStatus status, String pgTransactionId, boolean duplicate) {
        static Result from(Payment payment, boolean duplicate) {
            return new Result(payment.getId(), payment.getStatus(), payment.getPgTransactionId(), duplicate);
        }
    }
}
