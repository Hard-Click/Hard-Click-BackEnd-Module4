package com.wanted.backend.domain.payment.domain.model;

import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private final Long memberId;
    private final Long courseId;
    private final Integer amount;
    private PaymentStatus status;
    private final String idempotencyKey;
    private String pgTransactionId;
    private LocalDateTime paidAt;

    private Payment(Long id, Long memberId, Long courseId, Integer amount, PaymentStatus status,
                     String idempotencyKey, String pgTransactionId, LocalDateTime paidAt) {
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = paidAt;
    }

    /**
     * 결제 요청이 막 들어온 시점의 신규 결제를 생성한다. (PENDING 상태)
     */
    public static Payment create(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        return new Payment(null, memberId, courseId, amount, PaymentStatus.PENDING, idempotencyKey, null, null);
    }

    /**
     * 영속화된 결제를 복원할 때 사용한다.
     */
    public static Payment reconstruct(Long id, Long memberId, Long courseId, Integer amount, PaymentStatus status,
                                       String idempotencyKey, String pgTransactionId, LocalDateTime paidAt) {
        return new Payment(id, memberId, courseId, amount, status, idempotencyKey, pgTransactionId, paidAt);
    }

    /**
     * PG 호출 전 PENDING 상태로 전환한다.
     */
    public void markPending() {
        this.status = PaymentStatus.PENDING;
    }

    /**
     * PG 승인 결과를 받아 결제를 확정한다.
     */
    public void confirm(String pgTransactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.PAID;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = paidAt;
    }

    /**
     * PG 호출 실패/타임아웃 시 결제를 실패 처리한다.
     */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Integer getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
