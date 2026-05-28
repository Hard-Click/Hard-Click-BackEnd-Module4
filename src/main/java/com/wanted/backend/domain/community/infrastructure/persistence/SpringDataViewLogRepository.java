package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SpringDataViewLogRepository
        extends JpaRepository<ViewLogJpaEntity, Long> {

    boolean existsByMemberIdAndPostIdAndViewedAtAfter(
            Long memberId, Long postId, LocalDateTime after);
}