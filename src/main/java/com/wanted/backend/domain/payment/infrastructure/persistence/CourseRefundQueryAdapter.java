package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.CourseRefundQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRefundQueryAdapter implements CourseRefundQueryPort {

    private final OrderItemJpaRepository orderItemRepository;
    private final OrderJpaRepository orderRepository;
    private final PaymentJpaRepository paymentRepository;

    @Override
    public Optional<CoursePaymentData> findByCourseIdAndMemberId(Long courseId, Long memberId) {
        // order_items → orders (filter by memberId) → payments
        List<Long> orderIds = orderItemRepository.findByCourseId(courseId).stream()
                .map(OrderItemJpaEntity::getOrderId)
                .toList();

        if (orderIds.isEmpty()) {
            return Optional.empty();
        }

        return orderRepository.findByIdIn(orderIds).stream()
                .filter(o -> memberId.equals(o.getMemberId()))
                .findFirst()
                .flatMap(order -> paymentRepository.findByOrderId(order.getId()))
                .map(p -> new CoursePaymentData(
                        p.getId(),
                        p.getOrderId(),
                        p.getPaidAt(),
                        p.getPaidAmount(),
                        p.getStatus()
                ));
    }
}
