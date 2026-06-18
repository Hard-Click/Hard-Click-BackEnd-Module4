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

    public static Payment create(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        validateCreate(memberId, courseId, amount, idempotencyKey);
        return new Payment(null, memberId, courseId, amount, PaymentStatus.PENDING, idempotencyKey, null, null);
    }

    public static Payment reconstruct(Long id, Long memberId, Long courseId, Integer amount, PaymentStatus status,
                                       String idempotencyKey, String pgTransactionId, LocalDateTime paidAt) {
        if (id == null) throw new IllegalArgumentException("id는 필수입니다.");
        if (status == null) throw new IllegalArgumentException("status는 필수입니다.");
        validateCreate(memberId, courseId, amount, idempotencyKey);
        return new Payment(id, memberId, courseId, amount, status, idempotencyKey, pgTransactionId, paidAt);
    }

    public void confirm(String pgTransactionId, LocalDateTime paidAt) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 결제 확정이 가능합니다. 현재 상태: " + this.status);
        }
        if (pgTransactionId == null || pgTransactionId.isBlank()) {
            throw new IllegalArgumentException("pgTransactionId는 필수입니다.");
        }
        if (paidAt == null) {
            throw new IllegalArgumentException("paidAt은 필수입니다.");
        }
        this.status = PaymentStatus.PAID;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = paidAt;
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 결제 실패 처리가 가능합니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
    }

    private static void validateCreate(Long memberId, Long courseId, Integer amount, String idempotencyKey) {
        if (memberId == null) throw new IllegalArgumentException("memberId는 필수입니다.");
        if (courseId == null) throw new IllegalArgumentException("courseId는 필수입니다.");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey는 필수입니다.");
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getCourseId() { return courseId; }
    public Integer getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getPgTransactionId() { return pgTransactionId; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
