package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    List<CommentJpaEntity> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId);

    //댓글 수 조회
    int countByPostId(Long postId);

    // 방법②: Batch GROUP BY — N번 COUNT → 1번 GROUP BY 쿼리
    @Query("SELECT c.postId AS postId, COUNT(c) AS cnt FROM CommentJpaEntity c WHERE c.postId IN :postIds GROUP BY c.postId")
    List<CommentCountRow> countsByPostIds(@Param("postIds") Collection<Long> postIds);

    interface CommentCountRow {
        Long getPostId();
        Long getCnt();
    }
}
