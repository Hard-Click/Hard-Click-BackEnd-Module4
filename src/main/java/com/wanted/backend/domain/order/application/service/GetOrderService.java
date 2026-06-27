package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.dto.OrderDetailResult;
import com.wanted.backend.domain.order.application.port.OrderCourseQueryPort;
import com.wanted.backend.domain.order.application.port.OrderEnrollmentStatusPort;
import com.wanted.backend.domain.order.application.port.OrderSubscriptionPlanPort;
import com.wanted.backend.domain.order.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;
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
    private final OrderCourseQueryPort orderCourseQueryPort;
    private final OrderSubscriptionPlanPort orderSubscriptionPlanPort;

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

        Map<Long, String> thumbnailByCourseId = courseIds.isEmpty()
                ? Map.of()
                : orderCourseQueryPort.findThumbnailUrlsByIds(courseIds);

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
                                    : enrollStatuses.getOrDefault(item.getCourseId(), "NONE");

                    String thumbnailUrl =
                            item.getCourseId() == null
                                    ? null
                                    : thumbnailByCourseId.getOrDefault(item.getCourseId(), null);

                    return new OrderDetailResult.Item(
                            item.getCourseId(),
                            item.getTitle(),
                            thumbnailUrl,
                            item.getPrice(),
                            refundable,
                            refundAmount,
                            item.isRefunded(),
                            enrollStatus
                    );
                })
                .toList();

        // 구독 주문은 order_items를 영속화하지 않으므로(courseId NOT NULL 제약) 플랜 정보로 항목을 합성한다.
        if (items.isEmpty() && order.getType() == OrderType.SUBSCRIPTION) {
            boolean refunded = order.getStatus() == OrderStatus.REFUNDED;
            String planName = orderSubscriptionPlanPort.getAnnualPass().name();
            items = List.of(new OrderDetailResult.Item(
                    null,
                    planName,
                    null,
                    order.getTotalAmount(),
                    orderPaid && !refunded,
                    refunded ? 0 : order.getTotalAmount(),
                    refunded,
                    null
            ));
        }

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