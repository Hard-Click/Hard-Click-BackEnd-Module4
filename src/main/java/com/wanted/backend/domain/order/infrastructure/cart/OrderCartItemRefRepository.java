package com.wanted.backend.domain.order.infrastructure.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderCartItemRefRepository extends JpaRepository<OrderCartItemRefEntity, Long> {

    List<OrderCartItemRefEntity> findByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM OrderCartItemRef e WHERE e.memberId = :memberId AND e.courseId IN :courseIds")
    void deleteByMemberIdAndCourseIdIn(@Param("memberId") Long memberId, @Param("courseIds") List<Long> courseIds);

    @Modifying
    @Query("DELETE FROM OrderCartItemRef e WHERE e.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
