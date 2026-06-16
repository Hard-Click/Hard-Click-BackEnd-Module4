package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.domain.model.Order;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final WritableOrderJpaRepository repository;

    @Override
    public Order save(Order order) {
        WritableOrderJpaEntity entity = WritableOrderJpaEntity.create(
                order.getMemberId(), order.getOrderNo(), order.getPaymentType(), order.getPlanId());
        WritableOrderJpaEntity saved = repository.save(entity);
        return Order.restore(saved.getId(), saved.getMemberId(), saved.getOrderNo(),
                saved.getPaymentType(), OrderStatus.PENDING, saved.getPlanId());
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return repository.findById(orderId)
                .map(e -> Order.restore(e.getId(), e.getMemberId(), e.getOrderNo(),
                        e.getPaymentType(), e.getStatus(), e.getPlanId()));
    }
}
