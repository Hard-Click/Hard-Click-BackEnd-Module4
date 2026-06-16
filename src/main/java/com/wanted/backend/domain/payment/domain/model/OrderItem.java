package com.wanted.backend.domain.payment.domain.model;

public class OrderItem {

    private Long id;
    private Long orderId;
    private Long courseId;
    private String courseTitle;
    private Integer price;

    private OrderItem() {}

    public static OrderItem create(Long courseId, String courseTitle, Integer price) {
        OrderItem item = new OrderItem();
        item.courseId = courseId;
        item.courseTitle = courseTitle;
        item.price = price;
        return item;
    }

    public static OrderItem restore(Long id, Long orderId, Long courseId) {
        OrderItem item = new OrderItem();
        item.id = id;
        item.orderId = orderId;
        item.courseId = courseId;
        return item;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public Integer getPrice() { return price; }
}
