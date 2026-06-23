package com.wanted.backend.domain.wishlist.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistMemberJpaRepository extends JpaRepository<WishlistMemberJpaEntity, Long> {
    List<WishlistMemberJpaEntity> findAllByIdIn(List<Long> ids);
}
