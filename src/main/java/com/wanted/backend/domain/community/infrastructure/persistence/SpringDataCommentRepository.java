package com.wanted.backend.domain.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface SpringDataCommentRepository
        extends JpaRepository<CommentJpaEntity, Long> {

    boolean existsByPostIdAndIsAcceptedTrue(Long postId);

    List<CommentJpaEntity> findByParentIdOrderByCreatedAtAsc(Long parentId);

    boolean existsByParentId(Long commentId);

    List<CommentJpaEntity> findByPostIdAndParentIdIsNullOrderByIsAcceptedDescCreatedAtAsc(Long postId);

    int countByPostId(Long postId);

    List<CommentJpaEntity> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId);

    // 대댓글 일괄 조회 — N+1 제거 (1 IN query)
    List<CommentJpaEntity> findByParentIdInOrderByCreatedAtAsc(Collection<Long> parentIds);

    // 게시글 목록용 댓글 수 일괄 조회 — N+1 제거 (1 GROUP BY query)
    @Query("SELECT c.postId AS postId, COUNT(c) AS cnt FROM CommentJpaEntity c WHERE c.postId IN :postIds GROUP BY c.postId")
    List<CommentCountRow> countsByPostIds(@Param("postIds") Collection<Long> postIds);

    interface CommentCountRow {
        Long getPostId();
        Long getCnt();
    }
}
