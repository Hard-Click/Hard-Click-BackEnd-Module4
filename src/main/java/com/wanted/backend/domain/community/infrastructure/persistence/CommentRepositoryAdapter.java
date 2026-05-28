package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import org.springframework.stereotype.Repository;

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
        if (comment.getId() != null) {
            CommentJpaEntity entity = repository.findById(comment.getId()).orElseThrow();

            // 수정
            entity.update(comment.getContent(), comment.getImageUrl(), comment.getUpdatedAt());

            // Soft Delete
            if (comment.isDeleted()) {
                entity.softDelete(comment.getUpdatedAt());
            }

            // 채택
            if (comment.isAccepted()) {
                entity.accept(comment.getUpdatedAt());
            }

            return toDomain(repository.save(entity));
        }

        CommentJpaEntity entity = new CommentJpaEntity(
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getParentId(),
                comment.getContent(),
                comment.isAccepted(),
                comment.isDeleted(),
                comment.getImageUrl(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
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
        return repository.findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(postId)
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


}