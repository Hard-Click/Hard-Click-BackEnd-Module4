package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.policy.CommentAcceptPolicy;
import com.wanted.backend.domain.community.application.usecase.CommentCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.infrastructure.file.FileUploadUtils;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Service
@Transactional
public class CommentCommandService implements CommentCommandUseCase {

    @Value("${community.image.comment-dir}")
    private String commentDir;

    @Value("${community.image.comment-url}")
    private String commentUrl;

    @Value("${community.image.max-size}")
    private long maxFileSize;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentAcceptPolicy commentAcceptPolicy;

    public CommentCommandService(CommentRepository commentRepository,
                                 PostRepository postRepository, CommentAcceptPolicy commentAcceptPolicy) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentAcceptPolicy = commentAcceptPolicy;
    }

    @Override
    public Long create(CreateCommentCommand command) {

        // [1단계] 게시글 존재 여부 확인
        postRepository.findById(command.postId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // [2단계] 대댓글인 경우 부모 댓글 존재 여부 + 재대댓글 방지
        if (command.parentId() != null) {
            Comment parent = commentRepository.findById(command.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            // 부모가 이미 대댓글이면 재대댓글 불가
            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.REPLY_DEPTH_EXCEEDED);
            }
        }

        // [3단계] 파일 저장 (파일 있을 때만)
        String imageUrl = null;
        if (command.file() != null && !command.file().isEmpty()) {
            try {
                String savedFileName = FileUploadUtils.saveFile(
                        command.file(), commentDir, maxFileSize);
                imageUrl = commentUrl + savedFileName;
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        // [4단계] 댓글 도메인 생성 및 저장
        Comment comment = Comment.create(
                command.postId(),
                command.memberId(),
                command.parentId(),
                command.content(),
                imageUrl
        );

        return commentRepository.save(comment).getId();
    }

    @Override
    public void accept(AcceptCommentCommand command) {

        // [1단계] 채택할 댓글 조회 → Comment 객체를 Policy에 전달
        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // [2단계] 채택 가능 여부 검증 → Policy에 위임 (Comment 객체 전달)
        Post post = commentAcceptPolicy.validate(command.memberId(), comment);

        // [3단계] 채택 처리 → 도메인이 담당
        comment.accept();
        commentRepository.save(comment);

        // [4단계] 게시글 채택 완료 처리 → 도메인이 담당
        post.markAsAccepted();
        postRepository.save(post);
    }
}