package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.CourseProgressRatePort;
import com.wanted.backend.domain.payment.application.port.OrderQueryPort;
import com.wanted.backend.domain.payment.application.port.PaymentByOrderQueryPort;
import com.wanted.backend.domain.payment.application.port.SubscriptionPlanQueryPort;
import com.wanted.backend.domain.payment.application.port.SubscriptionRefundPort;
import com.wanted.backend.domain.payment.application.usecase.GetOrderDetailUseCase;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderDetailService implements GetOrderDetailUseCase {

    private static final int REFUND_PERIOD_DAYS = 7;
    private static final double REFUND_MAX_PROGRESS_RATE = 0.10;

    private final OrderQueryPort orderQueryPort;
    private final PaymentByOrderQueryPort paymentByOrderQueryPort;
    private final CourseProgressRatePort courseProgressRatePort;
    private final SubscriptionPlanQueryPort subscriptionPlanQueryPort;
    private final SubscriptionRefundPort subscriptionRefundPort;

    @Override
    @Transactional(readOnly = true)
    public Result handle(Long orderId, Long memberId) {
        OrderQueryPort.OrderData order = orderQueryPort.findById(orderId);
        PaymentByOrderQueryPort.PaymentDetail payment = paymentByOrderQueryPort.findByOrderId(orderId)
                .orElse(null);

        LocalDateTime paidAt = payment != null ? payment.paidAt() : null;

        if (order.paymentType() == PaymentType.COURSE) {
            return buildCourseOrderDetail(order, payment, memberId, paidAt);
        } else {
            return buildSubscriptionOrderDetail(order, payment, memberId, paidAt);
        }
    }

    private Result buildCourseOrderDetail(OrderQueryPort.OrderData order,
                                           PaymentByOrderQueryPort.PaymentDetail payment,
                                           Long memberId, LocalDateTime paidAt) {
        LocalDateTime now = LocalDateTime.now();

        List<ItemResult> items = order.items().stream()
                .map(item -> {
                    double progress = courseProgressRatePort.getProgressRate(memberId, item.courseId());
                    long daysElapsed = paidAt != null ? ChronoUnit.DAYS.between(paidAt, now) : 999;
                    boolean eligible = daysElapsed <= REFUND_PERIOD_DAYS && progress < REFUND_MAX_PROGRESS_RATE;
                    return new ItemResult(item.courseId(), item.courseTitle(), item.price(),
                            progress, daysElapsed, eligible);
                })
                .toList();

        List<RefundItem> eligibleItems = items.stream()
                .filter(ItemResult::refundEligible)
                .map(i -> new RefundItem(i.courseId(), i.title(), i.price()))
                .toList();

        int totalRefund = eligibleItems.stream().mapToInt(RefundItem::refundAmount).sum();
        RefundInfo refundInfo = new RefundInfo(!eligibleItems.isEmpty(), eligibleItems, totalRefund);

        int totalAmount = order.items().stream().mapToInt(OrderQueryPort.OrderItemData::price).sum();

        return new Result(order.orderId(), order.orderNo(), order.status(), PaymentType.COURSE,
                paidAt, items, totalAmount, refundInfo);
    }

    private Result buildSubscriptionOrderDetail(OrderQueryPort.OrderData order,
                                                 PaymentByOrderQueryPort.PaymentDetail payment,
                                                 Long memberId, LocalDateTime paidAt) {
        String planName = "구독권";
        Integer planPrice = payment != null ? payment.paidAmount() : 0;

        if (order.planId() != null) {
            var plan = subscriptionPlanQueryPort.findById(order.planId());
            if (plan.isPresent()) {
                planName = plan.get().name();
                planPrice = plan.get().price();
            }
        }

        List<ItemResult> items = List.of(
                new ItemResult(null, planName, planPrice, 0.0, 0L, false));

        // 구독 환불 가능 여부 조회
        var subscriptionOpt = subscriptionRefundPort.findActiveByMemberId(memberId);
        RefundInfo refundInfo;
        if (subscriptionOpt.isPresent()) {
            var sub = subscriptionOpt.get();
            LocalDateTime now = LocalDateTime.now();
            long remainingDays = ChronoUnit.DAYS.between(now, sub.expiredAt());
            long totalDays = sub.startedAt() != null
                    ? ChronoUnit.DAYS.between(sub.startedAt(), sub.expiredAt()) : 365L;
            int refundAmount = totalDays > 0
                    ? (int) Math.floor((double) remainingDays / totalDays * sub.paidAmount()) : 0;
            boolean refundable = remainingDays > 0 && "ACTIVE".equals(sub.status());
            List<RefundItem> eligibleItems = refundable
                    ? List.of(new RefundItem(null, planName, refundAmount)) : List.of();
            refundInfo = new RefundInfo(refundable, eligibleItems, refundable ? refundAmount : 0);
        } else {
            refundInfo = new RefundInfo(false, List.of(), 0);
        }

        return new Result(order.orderId(), order.orderNo(), order.status(), PaymentType.SUBSCRIPTION,
                paidAt, items, planPrice, refundInfo);
    }
}
