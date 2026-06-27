package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.MyPaymentHistoryQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.infrastructure.subscription.PaymentSubscriptionReferenceEntity;
import com.wanted.backend.domain.payment.infrastructure.subscription.PaymentSubscriptionReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPaymentHistoryQueryAdapter implements MyPaymentHistoryQueryPort {

    private static final Set<String> VISIBLE_STATUSES = Set.of(
            "PAID", "PARTIAL_REFUNDED", "REFUNDED", "CANCELED"
    );

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final PaymentSubscriptionReferenceRepository subscriptionRepository;

    @Override
    public Page<MyPaymentHistoryData> findByMemberId(Long memberId, Pageable pageable) {
        Page<OrderJpaEntity> orders = orderRepository.findByMemberIdAndStatusIn(memberId, VISIBLE_STATUSES, pageable);
        if (orders.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderIds = orders.getContent().stream()
                .map(OrderJpaEntity::getId)
                .distinct()
                .toList();

        Map<Long, List<Long>> courseIdsByOrderId = findCourseIdsByOrderId(orderIds);
        Map<Long, Long> planIdByOrderId = findSubscriptionPlanIdByOrderId(orderIds);

        List<MyPaymentHistoryData> content = orders.getContent().stream()
                .map(order -> new MyPaymentHistoryData(
                        order.getId(),
                        order.getId(),
                        order.getOrderNo(),
                        order.getPaymentType(),
                        order.getFinalAmount(),
                        toPaymentStatus(order.getStatus()),
                        order.getPaidAt(),
                        courseIdsByOrderId.getOrDefault(order.getId(), List.of()),
                        planIdByOrderId.get(order.getId())
                ))
                .toList();

        return new PageImpl<>(content, pageable, orders.getTotalElements());
    }

    private Map<Long, List<Long>> findCourseIdsByOrderId(Collection<Long> orderIds) {
        return orderItemRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(
                        OrderItemJpaEntity::getOrderId,
                        Collectors.mapping(OrderItemJpaEntity::getCourseId, Collectors.toList())
                ));
    }

    private Map<Long, Long> findSubscriptionPlanIdByOrderId(Collection<Long> orderIds) {
        return subscriptionRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(
                        PaymentSubscriptionReferenceEntity::getOrderId,
                        PaymentSubscriptionReferenceEntity::getPlanId,
                        (first, second) -> first
                ));
    }

    private PaymentStatus toPaymentStatus(String orderStatus) {
        return switch (orderStatus) {
            case "PAID", "PARTIAL_REFUNDED" -> PaymentStatus.PAID;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            case "CANCELED" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.READY;
        };
    }
}
