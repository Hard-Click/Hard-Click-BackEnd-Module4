package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentRevocationPort;
import com.wanted.backend.domain.order.application.usecase.RefundOrderItemUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.domain.payment.application.port.PgClient;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 주문 항목 단위 환불. 실제 Toss 결제취소(/v1/payments/{paymentKey}/cancel) 호출 후
 * 수강 권한 박탈 + 주문/항목 상태 갱신을 처리한다.
 *
 * 락: order:refund:lock:{idempotencyKey} (TTL 30초, SETNX) + 주문 행 비관적 락(findByIdForUpdate)으로
 * 동시에 같은 주문의 다른 항목을 환불해도 newStatus 계산이 race 없이 직렬화된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundOrderItemService implements RefundOrderItemUseCase {

    private static final String CANCEL_REASON = "학생 요청에 의한 강의 환불";
    private static final String LOCK_KEY_PREFIX = "order:refund:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final OrderRepository orderRepository;
    private final OrderEnrollmentRevocationPort enrollmentRevocationPort;
    private final PgClient pgClient;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void refund(Long memberId, Long orderId, Long courseId, String idempotencyKey) {
        String lockKey = LOCK_KEY_PREFIX + idempotencyKey;
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL);
        if (acquired == null || !acquired) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }

        try {
            Order order = orderRepository.findByIdForUpdate(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            if (!order.getMemberId().equals(memberId)) {
                throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
            }

            if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PARTIAL_REFUNDED) {
                throw new BusinessException(ErrorCode.ORDER_NOT_REFUNDABLE);
            }

            OrderItem item = order.getItems().stream()
                    .filter(i -> courseId.equals(i.getCourseId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));

            // 동일 idempotencyKey로 재시도된 요청이면 이미 처리된 결과이므로 조용히 성공 처리(멱등)
            if (item.isRefunded()) {
                return;
            }

            boolean allOthersAlreadyRefunded = order.getItems().stream()
                    .filter(i -> !courseId.equals(i.getCourseId()))
                    .allMatch(OrderItem::isRefunded);
            OrderStatus newStatus = allOthersAlreadyRefunded ? OrderStatus.REFUNDED : OrderStatus.PARTIAL_REFUNDED;

            try {
                pgClient.cancel(order.getPaymentKey(), item.getPrice(), CANCEL_REASON);
            } catch (RuntimeException e) {
                throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
            }

            orderRepository.refundItem(orderId, courseId, newStatus);
            enrollmentRevocationPort.revoke(memberId, courseId);
        } finally {
            try {
                redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), lockValue);
            } catch (RuntimeException e) {
                log.error("[REFUND_LOCK_RELEASE_FAILED] orderId: {}, lockKey: {}", orderId, lockKey, e);
            }
        }
    }
}
