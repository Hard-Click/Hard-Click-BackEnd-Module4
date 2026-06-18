package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.command.DeleteCommentCommand;
import com.wanted.backend.domain.community.application.command.UpdateCommentCommand;
import com.wanted.backend.domain.community.application.policy.CommentAcceptPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.usecase.CommentCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CommentCommandService implements CommentCommandUseCase {

    @Value("${community.image.max-size}")
    private long maxFileSize;

    private final CommunityFileStoragePort storagePort;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentAcceptPolicy commentAcceptPolicy;

    public CommentCommandService(CommunityFileStoragePort storagePort,
                                 CommentRepository commentRepository,
                                 PostRepository postRepository,
                                 CommentAcceptPolicy commentAcceptPolicy) {
        this.storagePort = storagePort;
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

            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.REPLY_DEPTH_EXCEEDED);
            }

            if (!parent.getPostId().equals(command.postId())) {
                throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
            }
        }

        // [3단계] 파일 S3 업로드 (파일 있을 때만)
        String imageUrl = null;
        if (command.file() != null && !command.file().isEmpty()) {
            imageUrl = storagePort.store(command.file(), "comments", maxFileSize);
        }

        // [4단계] 댓글 도메인 생성 및 저장 — S3 업로드 성공 후 DB 실패 시 롤백
        final String uploadedUrl = imageUrl;
        try {
            Comment comment = Comment.create(
                    command.postId(),
                    command.memberId(),
                    command.parentId(),
                    command.content(),
                    imageUrl
            );
            return commentRepository.save(comment).getId();
        } catch (Exception e) {
            if (uploadedUrl != null) {
                storagePort.delete(uploadedUrl);
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void accept(AcceptCommentCommand command) {
        // [1단계] 채택할 댓글 조회
        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // [2단계] 채택 가능 여부 검증 → Policy에 위임
        Post post = commentAcceptPolicy.validate(command.memberId(), comment);

        // [3단계] 채택 처리 → 도메인이 담당
        comment.accept();
        commentRepository.accept(comment.getId(), comment.getUpdatedAt());

        // [4단계] 게시글 채택 완료 처리 → 도메인이 담당
        post.markAsAccepted();
        postRepository.save(post);
    }

    @Override
    public void update(UpdateCommentCommand command) {
        // [1단계] 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // [2단계] 본인 댓글 여부 + 채택 여부 검증 → 도메인이 담당
        comment.validateUpdatable(command.memberId());

        // [3단계] 새 파일 S3 업로드 먼저 — 업로드 성공 후 기존 파일 삭제
        String imageUrl = null;
        if (command.file() != null && !command.file().isEmpty()) {
            imageUrl = storagePort.store(command.file(), "comments", maxFileSize);
        }

        // [4단계] 기존 파일 S3 삭제 — 새 업로드 성공 후에 삭제
        if (comment.getImageUrl() != null) {
            storagePort.delete(comment.getImageUrl());
        }

        // [5단계] 댓글 수정 → 도메인이 담당
        comment.update(command.content(), imageUrl);
        commentRepository.update(comment);
    }

    @Override
    public void delete(DeleteCommentCommand command) {
        // [1단계] 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // [2단계] 본인 댓글 여부 + 채택 여부 검증 → 도메인이 담당
        comment.validateDeletable(command.memberId());

        // [3단계] 파일 S3 삭제 (파일 있을 때만)
        if (comment.getImageUrl() != null) {
            storagePort.delete(comment.getImageUrl());
        }

        // [4단계] 대댓글 존재 여부에 따라 삭제 방식 결정
        boolean hasReplies = commentRepository.existsByParentId(command.commentId());

        if (hasReplies) {
            comment.softDelete();
            commentRepository.softDelete(comment.getId(), comment.getUpdatedAt());
        } else {
            commentRepository.deleteById(command.commentId());
        }
    }
}
