package com.wanted.backend.domain.payment.domain.model;

import java.time.LocalDateTime;

public class Refund {

    private Long id;
    private Long memberId;
    private Long paymentId;
    private Long enrollmentId;
    private Long subscriptionId;
    private RefundType refundType;
    private String reason;
    private Integer refundAmount;
    private RefundStatus status;
    private LocalDateTime refundedAt;

    private Refund() {}

    public static Refund createCourseRefund(Long memberId, Long paymentId, Long enrollmentId,
                                            String reason, Integer refundAmount) {
        Refund refund = new Refund();
        refund.memberId = memberId;
        refund.paymentId = paymentId;
        refund.enrollmentId = enrollmentId;
        refund.refundType = RefundType.COURSE;
        refund.reason = reason;
        refund.refundAmount = refundAmount;
        refund.status = RefundStatus.COMPLETED;
        refund.refundedAt = LocalDateTime.now();
        return refund;
    }

    public static Refund createSubscriptionRefund(Long memberId, Long paymentId, Long subscriptionId,
                                                   String reason, Integer refundAmount) {
        Refund refund = new Refund();
        refund.memberId = memberId;
        refund.paymentId = paymentId;
        refund.subscriptionId = subscriptionId;
        refund.refundType = RefundType.SUBSCRIPTION;
        refund.reason = reason;
        refund.refundAmount = refundAmount;
        refund.status = RefundStatus.COMPLETED;
        refund.refundedAt = LocalDateTime.now();
        return refund;
    }

    public static Refund restore(Long id, Long memberId, Long paymentId, Long enrollmentId,
                                  Long subscriptionId, RefundType refundType, String reason,
                                  Integer refundAmount, RefundStatus status, LocalDateTime refundedAt) {
        Refund refund = new Refund();
        refund.id = id;
        refund.memberId = memberId;
        refund.paymentId = paymentId;
        refund.enrollmentId = enrollmentId;
        refund.subscriptionId = subscriptionId;
        refund.refundType = refundType;
        refund.reason = reason;
        refund.refundAmount = refundAmount;
        refund.status = status;
        refund.refundedAt = refundedAt;
        return refund;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getPaymentId() { return paymentId; }
    public Long getEnrollmentId() { return enrollmentId; }
    public Long getSubscriptionId() { return subscriptionId; }
    public RefundType getRefundType() { return refundType; }
    public String getReason() { return reason; }
    public Integer getRefundAmount() { return refundAmount; }
    public RefundStatus getStatus() { return status; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
}
