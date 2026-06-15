package com.wanted.backend.domain.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberStatusHistoryJpaRepository extends JpaRepository<MemberStatusHistoryJpaEntity, Long> {
}
