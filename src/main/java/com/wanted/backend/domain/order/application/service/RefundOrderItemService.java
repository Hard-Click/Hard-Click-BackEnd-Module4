package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentRevocationPort;
import com.wanted.backend.domain.order.application.usecase.RefundOrderItemUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 항목 단위 환불. 실제 PG(Toss) 결제취소 API 연동은 별도 단계(Stage 3)로 분리되어 있어
 * 이 단계에서는 수강 권한 박탈 + 주문/항목 상태 갱신까지만 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefundOrderItemService implements RefundOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final OrderEnrollmentRevocationPort enrollmentRevocationPort;

    @Override
    public void refund(Long memberId, Long orderId, Long courseId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PARTIAL_REFUNDED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_REFUNDABLE);
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> courseId.equals(i.getCourseId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (item.isRefunded()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_ALREADY_REFUNDED);
        }

        boolean allOthersAlreadyRefunded = order.getItems().stream()
                .filter(i -> !courseId.equals(i.getCourseId()))
                .allMatch(OrderItem::isRefunded);
        OrderStatus newStatus = allOthersAlreadyRefunded ? OrderStatus.REFUNDED : OrderStatus.PARTIAL_REFUNDED;

        orderRepository.refundItem(orderId, courseId, newStatus);
        enrollmentRevocationPort.revoke(memberId, courseId);
    }
}
