package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Page<PaymentJpaEntity> findByMemberId(Long memberId, Pageable pageable);

    java.util.Optional<PaymentJpaEntity> findByOrderId(Long orderId);

    @Modifying
    @Query("UPDATE PaymentJpaEntity p SET p.status = :status WHERE p.id = :paymentId")
    void updateStatus(@Param("paymentId") Long paymentId, @Param("status") String status);
}
