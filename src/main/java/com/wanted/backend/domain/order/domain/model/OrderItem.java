package com.wanted.backend.domain.order.domain.model;

/**
 * 주문 항목.
 * COURSE 주문은 courseId가 채워지고, SUBSCRIPTION 주문은 별도 항목을 영속화하지 않고
 * 조회 시 플랜 정보로 합성한다(현재 order_items.course_id가 NOT NULL이기 때문).
 */
public class OrderItem {

    private Long id;
    private final Long courseId;
    private final String title;
    private final int price;
    private boolean refunded;

    private OrderItem(Long id, Long courseId, String title, int price, boolean refunded) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.price = price;
        this.refunded = refunded;
    }

    public static OrderItem create(Long courseId, String title, int price) {
        return new OrderItem(null, courseId, title, price, false);
    }

    public static OrderItem restore(Long id, Long courseId, String title, int price, boolean refunded) {
        return new OrderItem(id, courseId, title, price, refunded);
    }

    public void markRefunded() {
        this.refunded = true;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public int getPrice() { return price; }
    public boolean isRefunded() { return refunded; }
}
