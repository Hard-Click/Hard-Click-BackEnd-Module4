package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.dto.OrderDetailResult;
import com.wanted.backend.domain.order.application.port.OrderEnrollmentStatusPort;
import com.wanted.backend.domain.order.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEnrollmentStatusPort orderEnrollmentStatusPort;

    @Override
    public OrderDetailResult getOrder(Long memberId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        List<Long> courseIds = order.getItems().stream()
                .map(OrderItem::getCourseId)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<Long, String> enrollStatuses = courseIds.isEmpty()
                ? Map.of()
                : orderEnrollmentStatusPort.findEnrollStatuses(memberId, courseIds);

        boolean orderPaid =
                order.getStatus() == OrderStatus.PAID
                        || order.getStatus() == OrderStatus.PARTIAL_REFUNDED;

        List<OrderDetailResult.Item> items = order.getItems().stream()
                .map(item -> {

                    boolean refundable =
                            orderPaid && !item.isRefunded();

                    int refundAmount =
                            item.isRefunded() ? 0 : item.getPrice();

                    String enrollStatus =
                            item.getCourseId() == null
                                    ? null
                                    : enrollStatuses.getOrDefault(
                                    item.getCourseId(),
                                    "NONE"
                            );

                    return new OrderDetailResult.Item(
                            item.getCourseId(),
                            item.getTitle(),
                            item.getPrice(),
                            refundable,
                            refundAmount,
                            item.isRefunded(),
                            enrollStatus
                    );
                })
                .toList();

        return new OrderDetailResult(
                order.getOrderNo(),
                order.getStatus(),
                order.getType(),
                order.getOrderedAt(),
                order.getPaidAt(),
                items,
                order.getTotalAmount()
        );
    }
}