package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface SpringDataCommentRepository
        extends JpaRepository<CommentJpaEntity, Long> {}