package com.wanted.backend.domain.cart.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity, Long> {

    List<CartItemJpaEntity> findByMemberId(Long memberId);

    Optional<CartItemJpaEntity> findByMemberIdAndCourseId(Long memberId, Long courseId);

    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);

    @Modifying
    @Query("DELETE FROM CartItemJpaEntity c WHERE c.memberId = :memberId AND c.courseId = :courseId")
    void deleteByMemberIdAndCourseId(@Param("memberId") Long memberId, @Param("courseId") Long courseId);

    @Modifying
    @Query("DELETE FROM CartItemJpaEntity c WHERE c.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
