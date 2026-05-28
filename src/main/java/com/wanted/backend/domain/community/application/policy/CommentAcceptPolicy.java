package com.wanted.backend.domain.community.application.policy;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommentAcceptPolicy {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentAcceptPolicy(PostRepository postRepository,
                               CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }


    public Post validate(Long memberId, Comment comment) {

        // [1단계] 대댓글 채택 불가
        // parentId가 있으면 대댓글
        if (comment.getParentId() != null) {
            throw new BusinessException(ErrorCode.REPLY_CANNOT_BE_ACCEPTED);
        }

        // [2단계] 게시글 존재 여부 확인
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // [3단계] 질문게시판 여부 확인
        if (post.getBoardType() != BoardType.QUESTION) {
            throw new BusinessException(ErrorCode.ACCEPT_NOT_ALLOWED);
        }

        // [4단계] 게시글 작성자만 채택 가능
        if (!post.getAuthorId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCEPT_NOT_AUTHORIZED);
        }

        // [5단계] 중복 채택 방지
        if (commentRepository.existsByPostIdAndIsAcceptedTrue(comment.getPostId())) {
            throw new BusinessException(ErrorCode.ALREADY_ACCEPTED);
        }

        return post;
    }
}