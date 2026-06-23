package com.wanted.backend.domain.wishlist.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistItemJpaRepository extends JpaRepository<WishlistItemJpaEntity, Long> {

    List<WishlistItemJpaEntity> findByMemberId(Long memberId);

    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);

    @Modifying
    @Query("DELETE FROM WishlistItemJpaEntity w WHERE w.memberId = :memberId AND w.courseId = :courseId")
    void deleteByMemberIdAndCourseId(@Param("memberId") Long memberId, @Param("courseId") Long courseId);
}
