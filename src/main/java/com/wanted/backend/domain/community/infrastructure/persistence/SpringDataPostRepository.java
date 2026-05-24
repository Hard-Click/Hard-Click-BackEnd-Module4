package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {}