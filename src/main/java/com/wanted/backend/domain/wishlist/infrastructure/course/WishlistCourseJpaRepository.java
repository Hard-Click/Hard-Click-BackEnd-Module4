package com.wanted.backend.domain.wishlist.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistCourseJpaRepository extends JpaRepository<WishlistCourseJpaEntity, Long> {
    List<WishlistCourseJpaEntity> findAllByIdIn(List<Long> ids);
}
