package com.wanted.backend.domain.order.application.service;

import com.wanted.backend.domain.order.application.dto.CheckoutResult;
import com.wanted.backend.domain.order.application.port.OrderCartQueryPort;
import com.wanted.backend.domain.order.application.port.OrderCourseQueryPort;
import com.wanted.backend.domain.order.application.port.OrderEnrollmentStatusPort;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService implements CheckoutUseCase {

    private static final DateTimeFormatter ORDER_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 재구매를 차단할 활성 수강 상태(환불 REFUNDED·만료 EXPIRED는 재구매 허용)
    private static final Set<String> ACTIVE_ENROLLMENT_STATUSES = Set.of("ENROLLED", "IN_PROGRESS", "COMPLETED");

    private final OrderRepository orderRepository;
    private final OrderCourseQueryPort orderCourseQueryPort;
    private final OrderCartQueryPort orderCartQueryPort;
    private final OrderSubscriptionPlanPort orderSubscriptionPlanPort;
    private final OrderEnrollmentStatusPort orderEnrollmentStatusPort;
    private final Clock clock;

    @Override
    public CheckoutResult checkout(Long memberId, OrderType type, Long courseId, List<Long> selectedCourseIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        return type == OrderType.SUBSCRIPTION
                ? checkoutSubscription(memberId, now)
                : checkoutCourse(memberId, courseId, selectedCourseIds, now);
    }

    private CheckoutResult checkoutCourse(
            Long memberId,
            Long courseId,
            List<Long> selectedCourseIds,
            LocalDateTime now
    ) {

        List<Long> courseIds;

        if (selectedCourseIds != null && !selectedCourseIds.isEmpty()) {

            List<Long> cartCourseIds = orderCartQueryPort.findCartCourseIds(memberId);

            // 중복 제거
            courseIds = selectedCourseIds.stream()
                    .distinct()
                    .toList();

            // 하나라도 장바구니에 없으면 실패
            if (!cartCourseIds.containsAll(courseIds)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

        } else if (courseId != null) {

            courseIds = List.of(courseId);

        } else {

            courseIds = orderCartQueryPort.findCartCourseIds(memberId);

        }

        if (courseIds.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_CHECKOUT);
        }

        List<OrderCourseQueryPort.CourseInfo> courses =
                orderCourseQueryPort.findAllByIds(courseIds);

        // 조회되지 않은 강의가 하나라도 있으면 실패
        if (courses.size() != courseIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Set<Long> foundCourseIds = courses.stream()
                .map(OrderCourseQueryPort.CourseInfo::courseId)
                .collect(Collectors.toSet());

        if (!foundCourseIds.containsAll(courseIds)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 활성 수강(ENROLLED/IN_PROGRESS/COMPLETED)만 재구매 차단 — 환불(REFUNDED)/만료(EXPIRED)는 재구매 허용
        Map<Long, String> enrollStatuses = orderEnrollmentStatusPort.findEnrollStatuses(memberId, courseIds);
        boolean alreadyEnrolled = enrollStatuses.values().stream()
                .anyMatch(ACTIVE_ENROLLMENT_STATUSES::contains);
        if (alreadyEnrolled) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }

        List<OrderItem> items = courses.stream()
                .map(c -> OrderItem.create(
                        c.courseId(),
                        c.title(),
                        c.price()))
                .toList();

        int total = items.stream()
                .mapToInt(OrderItem::getPrice)
                .sum();

        Order order = orderRepository.save(
                Order.create(
                        generateOrderNo(now),
                        memberId,
                        OrderType.COURSE,
                        total,
                        total,
                        now,
                        items
                ));

        return toResult(order);
    }

    private CheckoutResult checkoutSubscription(Long memberId, LocalDateTime now) {

        OrderSubscriptionPlanPort.PlanInfo plan =
                orderSubscriptionPlanPort.getAnnualPass();

        Order order = orderRepository.save(
                Order.create(
                        generateOrderNo(now),
                        memberId,
                        OrderType.SUBSCRIPTION,
                        plan.price(),
                        plan.price(),
                        now,
                        List.of()
                ));

        return new CheckoutResult(
                order.getOrderNo(),
                order.getType(),
                order.getStatus(),
                List.of(
                        new CheckoutResult.Item(
                                null,
                                plan.name(),
                                plan.price()
                        )
                ),
                order.getTotalAmount(),
                order.getFinalAmount()
        );
    }

    private CheckoutResult toResult(Order order) {
        List<CheckoutResult.Item> items = order.getItems().stream()
                .map(i -> new CheckoutResult.Item(
                        i.getCourseId(),
                        i.getTitle(),
                        i.getPrice()))
                .toList();

        return new CheckoutResult(
                order.getOrderNo(),
                order.getType(),
                order.getStatus(),
                items,
                order.getTotalAmount(),
                order.getFinalAmount()
        );
    }

    private String generateOrderNo(LocalDateTime now) {
        return "ORD-"
                + now.format(ORDER_NO_DATE)
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}