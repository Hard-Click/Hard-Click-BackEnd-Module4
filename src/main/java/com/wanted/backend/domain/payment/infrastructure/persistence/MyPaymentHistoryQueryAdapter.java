package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.MyPaymentHistoryQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import com.wanted.backend.domain.payment.infrastructure.subscription.PaymentSubscriptionReferenceEntity;
import com.wanted.backend.domain.payment.infrastructure.subscription.PaymentSubscriptionReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPaymentHistoryQueryAdapter implements MyPaymentHistoryQueryPort {

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final PaymentSubscriptionReferenceRepository subscriptionRepository;

    @Override
    public List<MyPaymentHistoryData> findByMemberId(Long memberId) {
        List<PaymentJpaEntity> payments = paymentRepository.findByMemberId(memberId);
        if (payments.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = payments.stream()
                .map(PaymentJpaEntity::getOrderId)
                .distinct()
                .toList();

        Map<Long, OrderJpaEntity> orderById = orderRepository.findByIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderJpaEntity::getId, Function.identity()));
        Map<Long, List<Long>> courseIdsByOrderId = findCourseIdsByOrderId(orderIds);
        Map<Long, Long> planIdByOrderId = findSubscriptionPlanIdByOrderId(orderIds);

        return payments.stream()
                .map(payment -> toData(
                        payment,
                        orderById.get(payment.getOrderId()),
                        courseIdsByOrderId.getOrDefault(payment.getOrderId(), List.of()),
                        planIdByOrderId.get(payment.getOrderId())
                ))
                .toList();
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

    private MyPaymentHistoryData toData(
            PaymentJpaEntity payment,
            OrderJpaEntity order,
            List<Long> courseIds,
            Long subscriptionPlanId
    ) {
        PaymentType paymentType = order == null ? null : order.getPaymentType();

        return new MyPaymentHistoryData(
                payment.getId(),
                payment.getOrderId(),
                order == null ? null : order.getOrderNo(),
                paymentType,
                payment.getPaidAmount(),
                PaymentStatus.from(payment.getStatus()),
                payment.getPaidAt(),
                courseIds,
                subscriptionPlanId
        );
    }
}
