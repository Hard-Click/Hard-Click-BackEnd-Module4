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
    public void refundItem(Long orderId, Long courseId, OrderStatus newOrderStatus) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        orderEntity.updateStatus(newOrderStatus);

        OrderItemEntity itemEntity = orderItemRepository.findByOrderIdAndCourseId(orderId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        if (itemEntity.isRefunded()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_ALREADY_REFUNDED);
        }
        itemEntity.markRefunded();
    }

    private Order toDomain(OrderEntity e, List<OrderItemEntity> itemEntities) {
        List<OrderItem> items = itemEntities.stream()
                .map(i -> OrderItem.restore(i.getId(), i.getCourseId(), i.getTitle(), i.getPrice(), i.isRefunded()))
                .toList();
        return Order.restore(
                e.getId(), e.getOrderNo(), e.getMemberId(), e.getType(),
                OrderStatus.valueOf(e.getStatus()),
                e.getTotalAmount(), e.getFinalAmount(), e.getOrderedAt(), e.getPaidAt(),
                e.getPaymentKey(), items);
    }
}
