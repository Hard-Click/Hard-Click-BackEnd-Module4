package com.wanted.backend.domain.payment.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentMemberReferenceRepository extends JpaRepository<PaymentMemberReferenceEntity, Long> {
    List<PaymentMemberReferenceEntity> findByIdIn(Collection<Long> ids);
}
