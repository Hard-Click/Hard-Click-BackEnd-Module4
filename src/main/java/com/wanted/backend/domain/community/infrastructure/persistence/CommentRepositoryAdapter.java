package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.CommentStatus;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryAdapter implements CommentRepository {

    private final SpringDataCommentRepository repository;

    public CommentRepositoryAdapter(SpringDataCommentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity entity = new CommentJpaEntity(
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getParentId(),
                comment.getContent(),
                comment.isAccepted(),
                comment.isDeleted(),
                comment.getStatus(),
                comment.getImageUrl(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
    }

    @Override
    public void update(Comment comment) {
        CommentJpaEntity entity = repository.findById(comment.getId()).orElseThrow();
        entity.update(comment.getContent(), comment.getImageUrl(), comment.getUpdatedAt());
        repository.save(entity);
    }

    @Override
    public void softDelete(Long commentId, LocalDateTime updatedAt) {
        CommentJpaEntity entity = repository.findById(commentId).orElseThrow();
        entity.softDelete(updatedAt);
        repository.save(entity);
    }

    @Override
    public void softDeleteByAdmin(Long commentId, LocalDateTime updatedAt) {
        CommentJpaEntity entity = repository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        entity.softDeleteByAdmin(updatedAt);
        repository.save(entity);
    }

    @Override
    public void accept(Long commentId, LocalDateTime updatedAt) {
        CommentJpaEntity entity = repository.findById(commentId).orElseThrow();
        entity.accept(updatedAt);
        repository.save(entity);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return repository.findById(commentId).map(this::toDomain);
    }

    private Comment toDomain(CommentJpaEntity entity) {
        return Comment.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getAuthorId(),
                entity.getParentId(),
                entity.getContent(),
                entity.isAccepted(),
                entity.isDeleted(),
                entity.getStatus(),
                entity.getImageUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public boolean existsByPostIdAndIsAcceptedTrue(Long postId) {
        return repository.existsByPostIdAndIsAcceptedTrue(postId);
    }

    @Override
    public List<Comment> findByPostIdAndParentIdIsNull(Long postId) {
        return repository.findByPostIdAndParentIdIsNullOrderByIsAcceptedDescCreatedAtAsc(postId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Comment> findByParentId(Long parentId) {
        return repository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByParentId(Long commentId) {
        return repository.existsByParentId(commentId);
    }

    @Override
    public void deleteById(Long commentId) {
        repository.deleteById(commentId);
    }

    @Override
    public int countByPostId(Long postId) {
        return repository.countByPostId(postId);
    }

}
