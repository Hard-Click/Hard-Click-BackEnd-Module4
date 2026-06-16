package com.wanted.backend.domain.payment.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private Long id;
    private Long memberId;
    private String orderNo;
    private PaymentType paymentType;
    private OrderStatus status;
    private Long planId;
    private List<OrderItem> items;
    private LocalDateTime createdAt;

    private Order() {}

    public static Order createCourseOrder(Long memberId, String orderNo, List<OrderItem> items) {
        Order order = new Order();
        order.memberId = memberId;
        order.orderNo = orderNo;
        order.paymentType = PaymentType.COURSE;
        order.status = OrderStatus.PENDING;
        order.items = items;
        return order;
    }

    public static Order createSubscriptionOrder(Long memberId, String orderNo, Long planId) {
        Order order = new Order();
        order.memberId = memberId;
        order.orderNo = orderNo;
        order.paymentType = PaymentType.SUBSCRIPTION;
        order.status = OrderStatus.PENDING;
        order.planId = planId;
        return order;
    }

    public static Order restore(Long id, Long memberId, String orderNo, PaymentType paymentType,
                                 OrderStatus status, Long planId) {
        Order order = new Order();
        order.id = id;
        order.memberId = memberId;
        order.orderNo = orderNo;
        order.paymentType = paymentType;
        order.status = status;
        order.planId = planId;
        return order;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getOrderNo() { return orderNo; }
    public PaymentType getPaymentType() { return paymentType; }
    public OrderStatus getStatus() { return status; }
    public Long getPlanId() { return planId; }
    public List<OrderItem> getItems() { return items; }
}
