package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentRevocationPort;
import com.wanted.backend.domain.order.application.port.OrderSubscriptionCancelPort;
import com.wanted.backend.domain.order.application.usecase.RefundOrderUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.domain.payment.application.port.PgClient;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 주문 전체 환불(관리자). 실제 결제는 orders 테이블에 기록되므로 order 도메인에서 처리한다.
 *
 * 락: order:refund:lock:{orderId} (TTL 30초, SETNX). PG 취소는 @Transactional 밖에서 실행해
 * DB 커넥션을 점유하지 않고, DB 갱신은 refundAll(비관적 락) 내부의 짧은 트랜잭션에서 수행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrderService implements RefundOrderUseCase {

    private static final String CANCEL_REASON = "관리자 환불 처리";
    private static final String LOCK_KEY_PREFIX = "order:refund:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final OrderRepository orderRepository;
    private final OrderEnrollmentRevocationPort enrollmentRevocationPort;
    private final OrderSubscriptionCancelPort subscriptionCancelPort;
    private final PgClient pgClient;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void refundOrder(Long orderId) {
        String lockKey = LOCK_KEY_PREFIX + orderId;
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL);
        if (acquired == null || !acquired) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PARTIAL_REFUNDED) {
                throw new BusinessException(ErrorCode.ORDER_NOT_REFUNDABLE);
            }

            if (order.getType() == OrderType.SUBSCRIPTION) {
                refundSubscription(order);
            } else {
                refundCourse(order);
            }
        } finally {
            try {
                redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), lockValue);
            } catch (RuntimeException e) {
                log.error("[ADMIN_REFUND_LOCK_RELEASE_FAILED] orderId: {}, lockKey: {}", orderId, lockKey, e);
            }
        }
    }

    private void refundCourse(Order order) {
        List<OrderItem> toRefund = order.getItems().stream()
                .filter(item -> !item.isRefunded())
                .toList();
        if (toRefund.isEmpty()) {
            return; // 이미 전부 환불됨 (멱등)
        }

        int cancelAmount = toRefund.stream().mapToInt(OrderItem::getPrice).sum();

        // PG 취소 — @Transactional 밖. 취소 성공 후 DB 갱신 실패 시 운영 보정 대상(ERROR 로그)
        try {
            pgClient.cancel(order.getPaymentKey(), cancelAmount, CANCEL_REASON);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
        }

        orderRepository.refundAll(order.getId());
        for (OrderItem item : toRefund) {
            if (item.getCourseId() != null) {
                enrollmentRevocationPort.revoke(order.getMemberId(), item.getCourseId());
            }
        }
    }

    private void refundSubscription(Order order) {
        try {
            pgClient.cancel(order.getPaymentKey(), order.getFinalAmount(), CANCEL_REASON);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
        }

        orderRepository.refundAll(order.getId());

        // 결제는 이미 취소됨 — 구독 취소 실패는 운영 보정 대상(ERROR 로그)으로만 남기고 응답은 성공 유지
        try {
            subscriptionCancelPort.cancelByMemberId(order.getMemberId());
        } catch (RuntimeException e) {
            log.error("[ADMIN_REFUND_SUBSCRIPTION_CANCEL_FAILED] orderId: {}, memberId: {}",
                    order.getId(), order.getMemberId(), e);
        }
    }
}
