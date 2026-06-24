package com.wanted.backend.domain.order.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private Long id;
    private final String orderNo;
    private final Long memberId;
    private final OrderType type;
    private OrderStatus status;
    private final int totalAmount;
    private final int finalAmount;
    private final LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private final List<OrderItem> items;

    private Order(Long id, String orderNo, Long memberId, OrderType type, OrderStatus status,
                  int totalAmount, int finalAmount, LocalDateTime orderedAt, LocalDateTime paidAt,
                  List<OrderItem> items) {
        this.id = id;
        this.orderNo = orderNo;
        this.memberId = memberId;
        this.type = type;
        this.status = status;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.orderedAt = orderedAt;
        this.paidAt = paidAt;
        this.items = items;
    }

    public static Order create(String orderNo, Long memberId, OrderType type,
                               int totalAmount, int finalAmount, LocalDateTime orderedAt,
                               List<OrderItem> items) {
        return new Order(null, orderNo, memberId, type, OrderStatus.READY,
                totalAmount, finalAmount, orderedAt, null, items);
    }

    public static Order restore(Long id, String orderNo, Long memberId, OrderType type, OrderStatus status,
                                int totalAmount, int finalAmount, LocalDateTime orderedAt, LocalDateTime paidAt,
                                List<OrderItem> items) {
        return new Order(id, orderNo, memberId, type, status,
                totalAmount, finalAmount, orderedAt, paidAt, items);
    }

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Long getMemberId() { return memberId; }
    public OrderType getType() { return type; }
    public OrderStatus getStatus() { return status; }
    public int getTotalAmount() { return totalAmount; }
    public int getFinalAmount() { return finalAmount; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public List<OrderItem> getItems() { return items; }
}
