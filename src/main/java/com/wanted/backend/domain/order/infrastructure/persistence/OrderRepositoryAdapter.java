package com.wanted.backend.domain.order.infrastructure.persistence;

import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataOrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = new OrderEntity(
                order.getOrderNo(), order.getMemberId(), order.getType(), order.getStatus().name(),
                order.getTotalAmount(), order.getFinalAmount(), order.getOrderedAt(), order.getPaidAt());
        OrderEntity saved = orderRepository.save(entity);

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(i -> new OrderItemEntity(saved.getId(), i.getCourseId(), i.getTitle(), i.getPrice()))
                .toList();
        List<OrderItemEntity> savedItems = itemEntities.isEmpty()
                ? List.of()
                : orderItemRepository.saveAll(itemEntities);

        return toDomain(saved, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(e -> toDomain(e, orderItemRepository.findByOrderId(e.getId())));
    }

    @Override
    @Transactional
    public Optional<Order> findByIdForUpdate(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .map(e -> toDomain(e, orderItemRepository.findByOrderId(e.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .map(e -> toDomain(e, orderItemRepository.findByOrderId(e.getId())));
    }

    @Override
    @Transactional
    public void markPaid(String orderNo, LocalDateTime paidAt, String paymentKey) {
        OrderEntity entity = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        entity.markPaid(paidAt, paymentKey);
    }

    @Override
    @Transactional
    public void refundItem(Long orderId, Long courseId) {
        // 주문 row를 비관적 락으로 잡아 동일 주문의 동시 환불을 직렬화한다
        // (서로 다른 항목을 동시 환불할 때 주문 상태가 stale하게 계산되는 것을 방지).
        OrderEntity orderEntity = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        OrderItemEntity itemEntity = orderItemRepository.findByOrderIdAndCourseId(orderId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        if (itemEntity.isRefunded()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_ALREADY_REFUNDED);
        }
        itemEntity.markRefunded();

        // 방금 환불한 항목까지 반영된 현재 상태로 주문 상태 재계산
        boolean allRefunded = orderItemRepository.findByOrderId(orderId).stream()
                .allMatch(OrderItemEntity::isRefunded);
        orderEntity.updateStatus(allRefunded ? OrderStatus.REFUNDED : OrderStatus.PARTIAL_REFUNDED);
    }

    @Override
    @Transactional
    public void refundAll(Long orderId) {
        OrderEntity orderEntity = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        orderEntity.updateStatus(OrderStatus.REFUNDED);

        orderItemRepository.findByOrderId(orderId).stream()
                .filter(item -> !item.isRefunded())
                .forEach(OrderItemEntity::markRefunded);
    }

    private Order toDomain(OrderEntity e, List<OrderItemEntity> itemEntities) {
        List<OrderItem> items = itemEntities.stream()
                .map(i -> OrderItem.restore(i.getId(), i.getCourseId(), i.getTitle(), i.getPrice(), i.isRefunded()))
                .toList();
        return Order.restore(
                e.getId(), e.getOrderNo(), e.getMemberId(), e.getType(),
                parseOrderStatus(e.getStatus()),
                e.getTotalAmount(), e.getFinalAmount(), e.getOrderedAt(), e.getPaidAt(),
                e.getPaymentKey(), items);
    }

    private OrderStatus parseOrderStatus(String raw) {
        try {
            return OrderStatus.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_STATUS);
        }
    }
}
