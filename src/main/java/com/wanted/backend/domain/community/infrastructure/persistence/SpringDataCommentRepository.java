package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SpringDataCommentRepository
        extends JpaRepository<CommentJpaEntity, Long> {

    boolean existsByPostIdAndIsAcceptedTrue(Long postId);

    //원댓글 목록(최신순)
    List<CommentJpaEntity> findByParentIdOrderByCreatedAtAsc(Long parentId);

    //대댓글 목록(오래된 순)
    List<CommentJpaEntity> findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(Long postId);

    // 대댓글 존재 여부
    boolean existsByParentId(Long commentId);

}