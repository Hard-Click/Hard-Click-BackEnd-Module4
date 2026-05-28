package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataReviewRepository extends JpaRepository<ReviewJpaEntity, Long> {

    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);

    Optional<ReviewJpaEntity> findByCourseIdAndMemberId(Long courseId, Long memberId);

    List<ReviewJpaEntity> findByCourseIdAndMemberIdNot(Long courseId, Long memberId, Pageable pageable);

    List<ReviewJpaEntity> findByCourseId(Long courseId, Pageable pageable);

    int countByCourseId(Long courseId);

    List<ReviewJpaEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
