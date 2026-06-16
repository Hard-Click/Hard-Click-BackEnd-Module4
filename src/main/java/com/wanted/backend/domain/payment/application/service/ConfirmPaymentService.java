package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.command.ConfirmPaymentCommand;
import com.wanted.backend.domain.payment.application.port.EnrollmentCreatePort;
import com.wanted.backend.domain.payment.application.port.OrderQueryPort;
import com.wanted.backend.domain.payment.application.port.OrderStatusUpdatePort;
import com.wanted.backend.domain.payment.application.port.SubscriptionCreatePort;
import com.wanted.backend.domain.payment.application.usecase.ConfirmPaymentUseCase;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

    private final TossPaymentClient tossPaymentClient;
    private final OrderQueryPort orderQueryPort;
    private final OrderStatusUpdatePort orderStatusUpdatePort;
    private final PaymentCreatePort paymentCreatePort;
    private final EnrollmentCreatePort enrollmentCreatePort;
    private final SubscriptionCreatePort subscriptionCreatePort;

    @Override
    @Transactional
    public Result handle(ConfirmPaymentCommand command) {
        // command.orderId()는 Toss SDK의 orderId = 우리가 지정한 orderNo (예: "ORD-20260616-001")
        OrderQueryPort.OrderData order = orderQueryPort.findByOrderNo(command.orderId());

        if (order.status() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        TossPaymentClient.Response tossResponse = tossPaymentClient.confirm(
                command.paymentKey(), command.orderId(), command.amount());

        Long paymentId = paymentCreatePort.create(
                order.orderId(), command.memberId(), command.amount(),
                command.paymentKey(), tossResponse.approvedAt());

        orderStatusUpdatePort.updateStatus(order.orderId(), OrderStatus.PAID);

        if (order.paymentType() == PaymentType.COURSE) {
            List<Long> courseIds = order.items().stream()
                    .map(OrderQueryPort.OrderItemData::courseId)
                    .toList();
            enrollmentCreatePort.createAll(command.memberId(), courseIds);
        } else {
            LocalDateTime expiredAt = tossResponse.approvedAt().plusMonths(12);
            subscriptionCreatePort.create(command.memberId(), order.orderId(), order.planId(), expiredAt);
        }

        return new Result(paymentId, order.orderId(), order.orderNo(), command.amount(),
                order.paymentType(), tossResponse.approvedAt());
    }

    public interface PaymentCreatePort {
        Long create(Long orderId, Long memberId, Integer amount, String tossPaymentKey, LocalDateTime paidAt);
    }

    public interface TossPaymentClient {
        Response confirm(String paymentKey, String orderId, Integer amount);

        record Response(LocalDateTime approvedAt) {}
    }
}
