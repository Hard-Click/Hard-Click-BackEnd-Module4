package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    // boardType + 키워드 검색 + 페이징
    List<PostJpaEntity> findByBoardTypeAndTitleContainingAndStatus(
            BoardType boardType, String keyword, PostStatus status, Pageable pageable);

    // 전체 + 키워드 검색 + 페이징
    List<PostJpaEntity> findByTitleContainingAndStatus(
            String keyword, PostStatus status, Pageable pageable);

    // boardType + 키워드 검색 전체 수
    int countByBoardTypeAndTitleContainingAndStatus(
            BoardType boardType, String keyword, PostStatus status);

    // 전체 + 키워드 검색 전체 수
    int countByTitleContainingAndStatus(String keyword, PostStatus status);

    List<PostJpaEntity> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, PostStatus status);

    // 방법④: 댓글 생성/삭제 시 comment_count 동기화 — 원자적 UPDATE (조회 후 저장 방식의 레이스 방지)
    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);

}
