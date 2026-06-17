package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface CommentRepository {

    // 댓글 저장
    Comment save(Comment comment);

    //댓글 수정
    void update(Comment comment);

    //댓글 비공개 처리
    void softDelete(Long commentId, LocalDateTime updatedAt);

    void softDeleteByAdmin(Long commentId, LocalDateTime updatedAt);

    //댓글 채택
    void accept(Long commentId, LocalDateTime updatedAt);

    // 대댓글 작성 시 부모 댓글 존재 여부 + 재대댓글 방지용
    Optional<Comment> findById(Long commentId);

    // 게시글에 이미 채택된 댓글 존재 여부 (중복 채택 방지)
    boolean existsByPostIdAndIsAcceptedTrue(Long postId);

    //게시글 원댓글 목록 조회
    List<Comment> findByPostIdAndParentIdIsNull(Long postId);

    //게시글 대댓글 목록 조회
    List<Comment> findByParentId(Long parentId);

    // 대댓글 존재 여부 (삭제 방식 결정용)
    boolean existsByParentId(Long commentId);

    // Hard Delete
    void deleteById(Long commentId);

    //댓글 수 조회
    int countByPostId(Long postId);
}
