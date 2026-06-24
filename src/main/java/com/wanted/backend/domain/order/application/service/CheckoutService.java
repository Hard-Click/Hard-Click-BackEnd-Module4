package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.dto.CheckoutResult;
import com.wanted.backend.domain.order.application.port.OrderCartQueryPort;
import com.wanted.backend.domain.order.application.port.OrderCourseQueryPort;
import com.wanted.backend.domain.order.application.port.OrderSubscriptionPlanPort;
import com.wanted.backend.domain.order.application.usecase.CheckoutUseCase;
import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderItem;
import com.wanted.backend.domain.order.domain.model.OrderType;
import com.wanted.backend.domain.order.domain.repository.OrderRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService implements CheckoutUseCase {

    private static final DateTimeFormatter ORDER_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final OrderCourseQueryPort orderCourseQueryPort;
    private final OrderCartQueryPort orderCartQueryPort;
    private final OrderSubscriptionPlanPort orderSubscriptionPlanPort;
    private final Clock clock;

    @Override
    public CheckoutResult checkout(Long memberId, OrderType type, Long courseId) {
        LocalDateTime now = LocalDateTime.now(clock);
        return type == OrderType.SUBSCRIPTION
                ? checkoutSubscription(memberId, now)
                : checkoutCourse(memberId, courseId, now);
    }

    private CheckoutResult checkoutCourse(Long memberId, Long courseId, LocalDateTime now) {
        List<Long> courseIds = (courseId != null)
                ? List.of(courseId)
                : orderCartQueryPort.findCartCourseIds(memberId);

        if (courseIds.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CHECKOUT);
        }

        List<OrderCourseQueryPort.CourseInfo> courses = orderCourseQueryPort.findAllByIds(courseIds);
        if (courses.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CHECKOUT);
        }

        List<OrderItem> items = courses.stream()
                .map(c -> OrderItem.create(c.courseId(), c.title(), c.price()))
                .toList();
        int total = items.stream().mapToInt(OrderItem::getPrice).sum();

        Order order = orderRepository.save(
                Order.create(generateOrderNo(now), memberId, OrderType.COURSE, total, total, now, items));

        return toResult(order);
    }

    private CheckoutResult checkoutSubscription(Long memberId, LocalDateTime now) {
        OrderSubscriptionPlanPort.PlanInfo plan = orderSubscriptionPlanPort.getAnnualPass();

        // 구독 주문은 order_items를 영속화하지 않고 응답에서만 합성한다.
        Order order = orderRepository.save(
                Order.create(generateOrderNo(now), memberId, OrderType.SUBSCRIPTION,
                        plan.price(), plan.price(), now, List.of()));

        return new CheckoutResult(
                order.getOrderNo(),
                order.getType(),
                order.getStatus(),
                List.of(new CheckoutResult.Item(null, plan.name(), plan.price())),
                order.getTotalAmount(),
                order.getFinalAmount()
        );
    }

    private CheckoutResult toResult(Order order) {
        List<CheckoutResult.Item> items = order.getItems().stream()
                .map(i -> new CheckoutResult.Item(i.getCourseId(), i.getTitle(), i.getPrice()))
                .toList();
        return new CheckoutResult(
                order.getOrderNo(), order.getType(), order.getStatus(),
                items, order.getTotalAmount(), order.getFinalAmount());
    }

    private String generateOrderNo(LocalDateTime now) {
        return "ORD-" + now.format(ORDER_NO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
