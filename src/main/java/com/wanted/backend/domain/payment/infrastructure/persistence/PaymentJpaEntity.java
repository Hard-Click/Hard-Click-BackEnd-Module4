package com.wanted.backend.domain.payment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // 단건 강의/구독권 주문에 연결된 결제는 orderId를 갖는다 (기존 결제 내역 조회 플로우에서 사용).
    // 멱등키 기반 중복결제 방지 데모 플로우(PaymentFacade)는 주문 없이 courseId만으로 동작하므로 nullable이다.
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(nullable = false)
    private String status;

    @Column(name = "idempotency_key", unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "pg_transaction_id", length = 128)
    private String pgTransactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public PaymentJpaEntity(Long memberId, Long courseId, Integer paidAmount, String status, String idempotencyKey) {
        this.memberId = memberId;
        this.courseId = courseId;
        this.paidAmount = paidAmount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public void confirm(String status, String pgTransactionId, LocalDateTime paidAt) {
        this.status = status;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = paidAt;
    }

    public void markFailed(String status) {
        this.status = status;
    }
}
