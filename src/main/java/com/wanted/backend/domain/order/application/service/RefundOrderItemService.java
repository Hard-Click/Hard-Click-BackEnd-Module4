package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentRevocationPort;
import com.wanted.backend.domain.order.application.usecase.RefundOrderItemUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.domain.payment.application.port.PgClient;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 항목 단위 환불. 실제 Toss 결제취소(/v1/payments/{paymentKey}/cancel) 호출 후
 * 수강 권한 박탈 + 주문/항목 상태 갱신을 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefundOrderItemService implements RefundOrderItemUseCase {

    private static final String CANCEL_REASON = "학생 요청에 의한 강의 환불";

    private final OrderRepository orderRepository;
    private final OrderEnrollmentRevocationPort enrollmentRevocationPort;
    private final PgClient pgClient;

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

        try {
            pgClient.cancel(order.getPaymentKey(), item.getPrice(), CANCEL_REASON);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
        }

        orderRepository.refundItem(orderId, courseId, newStatus);
        enrollmentRevocationPort.revoke(memberId, courseId);
    }
}
