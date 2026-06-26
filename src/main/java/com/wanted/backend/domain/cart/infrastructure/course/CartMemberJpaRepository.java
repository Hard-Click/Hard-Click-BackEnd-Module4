package com.wanted.backend.domain.cart.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CartMemberJpaRepository extends JpaRepository<CartMemberJpaEntity, Long> {

    List<CartMemberJpaEntity> findByIdIn(Collection<Long> ids);
}
