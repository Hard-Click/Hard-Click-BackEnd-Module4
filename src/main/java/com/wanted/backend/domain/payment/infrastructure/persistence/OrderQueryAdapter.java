package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.OrderQueryPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {

    private final WritableOrderJpaRepository orderRepository;
    private final WritableOrderItemJpaRepository orderItemRepository;

    @Override
    public OrderData findById(Long orderId) {
        WritableOrderJpaEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        return toData(order);
    }

    @Override
    public OrderData findByOrderNo(String orderNo) {
        WritableOrderJpaEntity order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        return toData(order);
    }

    private OrderData toData(WritableOrderJpaEntity order) {
        List<WritableOrderItemJpaEntity> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemData> itemData = items.stream()
                .map(i -> new OrderItemData(i.getCourseId(), i.getCourseTitle(), i.getPrice()))
                .toList();
        return new OrderData(order.getId(), order.getOrderNo(), order.getPaymentType(),
                order.getStatus(), order.getPlanId(), itemData);
    }
}
