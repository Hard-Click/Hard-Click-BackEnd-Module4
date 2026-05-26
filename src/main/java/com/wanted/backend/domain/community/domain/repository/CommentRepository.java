package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Comment;
import java.util.Optional;


public interface CommentRepository {

    // 댓글 저장
    Comment save(Comment comment);

    // 대댓글 작성 시 부모 댓글 존재 여부 + 재대댓글 방지용
    Optional<Comment> findById(Long commentId);

    // 게시글에 이미 채택된 댓글 존재 여부 (중복 채택 방지)
    boolean existsByPostIdAndIsAcceptedTrue(Long postId);

}