package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WritableOrderJpaRepository extends JpaRepository<WritableOrderJpaEntity, Long> {

    java.util.Optional<WritableOrderJpaEntity> findByOrderNo(String orderNo);

    @Modifying
    @Query("UPDATE WritableOrder o SET o.status = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") Long orderId, @Param("status") com.wanted.backend.domain.payment.domain.model.OrderStatus status);
}
