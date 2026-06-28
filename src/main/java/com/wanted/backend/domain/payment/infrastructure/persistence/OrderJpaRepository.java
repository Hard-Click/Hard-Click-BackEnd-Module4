package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findByIdIn(Collection<Long> orderIds);

    Page<OrderJpaEntity> findByMemberIdAndStatusIn(Long memberId, Collection<String> statuses, Pageable pageable);
}
