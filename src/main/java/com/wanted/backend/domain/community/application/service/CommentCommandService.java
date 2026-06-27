package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.command.DeleteCommentCommand;
import com.wanted.backend.domain.community.application.command.UpdateCommentCommand;
import com.wanted.backend.domain.community.application.policy.CommentAcceptPolicy;
import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.usecase.CommentCommandUseCase;
import com.wanted.backend.domain.community.domain.event.CommentAcceptedEvent;
import com.wanted.backend.domain.community.domain.event.CommentReplyCreatedEvent;
import com.wanted.backend.domain.community.domain.event.PostCommentCreatedEvent;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final CommunityAccessPolicy communityAccessPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public CommentCommandService(CommunityFileStoragePort storagePort,
                                 CommentRepository commentRepository,
                                 PostRepository postRepository,
                                 CommentAcceptPolicy commentAcceptPolicy,
                                 CommunityAccessPolicy communityAccessPolicy,
                                 ApplicationEventPublisher eventPublisher) {
        this.storagePort = storagePort;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentAcceptPolicy = commentAcceptPolicy;
        this.communityAccessPolicy = communityAccessPolicy;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long create(CreateCommentCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (command.parentId() != null) {
            parent = commentRepository.findById(command.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.REPLY_DEPTH_EXCEEDED);
            }
            if (!parent.getPostId().equals(command.postId())) {
                throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
            }
        }

        String imageUrl = null;
        if (command.file() != null && !command.file().isEmpty()) {
            imageUrl = storagePort.store(command.file(), "comments", maxFileSize);
        }

        final String uploadedUrl = imageUrl;
        final Comment finalParent = parent;
        try {
            Comment comment = Comment.create(command.postId(), command.memberId(),
                    command.parentId(), command.content(), imageUrl);
            Comment saved = commentRepository.save(comment);

            // 이벤트 발행 — 트랜잭션 커밋 후 @Async 리스너가 처리
            if (finalParent != null) {
                eventPublisher.publishEvent(CommentReplyCreatedEvent.of(
                        finalParent.getAuthorId(), command.memberId(),
                        command.postId(), saved.getId()));
            } else {
                eventPublisher.publishEvent(PostCommentCreatedEvent.of(
                        post.getAuthorId(), command.memberId(),
                        command.postId(), saved.getId()));
            }

            return saved.getId();
        } catch (Exception e) {
            if (uploadedUrl != null) storagePort.delete(uploadedUrl);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void accept(AcceptCommentCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        Post post = commentAcceptPolicy.validate(command.memberId(), comment);

        comment.accept();
        commentRepository.accept(comment.getId(), comment.getUpdatedAt());

        post.markAsAccepted();
        postRepository.save(post);

        eventPublisher.publishEvent(CommentAcceptedEvent.of(
                comment.getAuthorId(), comment.getPostId(), comment.getId()));
    }

    @Override
    public void update(UpdateCommentCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        comment.validateUpdatable(command.memberId());

        String imageUrl = null;
        if (command.file() != null && !command.file().isEmpty()) {
            imageUrl = storagePort.store(command.file(), "comments", maxFileSize);
        }

        if (comment.getImageUrl() != null) {
            storagePort.delete(comment.getImageUrl());
        }

        comment.update(command.content(), imageUrl);
        commentRepository.update(comment);
    }

    @Override
    public void delete(DeleteCommentCommand command) {
        communityAccessPolicy.validateAccess(command.memberId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (command.isAdmin()) {
            if (comment.getImageUrl() != null) {
                storagePort.delete(comment.getImageUrl());
            }

            boolean hasReplies = commentRepository.existsByParentId(command.commentId());
            if (hasReplies || comment.isAccepted()) {
                // 대댓글이 있거나 채택된 댓글은 구조 보존을 위해 소프트 삭제
                comment.softDeleteByAdmin();
                commentRepository.softDeleteByAdmin(comment.getId(), comment.getUpdatedAt());
            } else {
                commentRepository.deleteById(command.commentId());
            }
            return;
        }

        comment.validateDeletable(command.memberId());

        if (comment.getImageUrl() != null) {
            storagePort.delete(comment.getImageUrl());
        }

        boolean hasReplies = commentRepository.existsByParentId(command.commentId());
        if (hasReplies) {
            comment.softDelete();
            commentRepository.softDelete(comment.getId(), comment.getUpdatedAt());
        } else {
            commentRepository.deleteById(command.commentId());
        }
    }
}