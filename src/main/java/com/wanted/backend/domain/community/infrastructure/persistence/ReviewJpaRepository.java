package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {

    Optional<ReviewJpaEntity> findByCourseIdAndMemberId(Long courseId, Long memberId);
}