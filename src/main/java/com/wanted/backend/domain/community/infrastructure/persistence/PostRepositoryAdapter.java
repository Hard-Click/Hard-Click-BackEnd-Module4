package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public class PostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository repository;

    public PostRepositoryAdapter(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = new PostJpaEntity(
                post.getAuthorId(), post.getBoardType(), post.getSubject(),
                post.getTitle(), post.getContent(), post.getViewCount(),
                post.isAccepted(), post.getCreatedAt(), post.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
    }

    @Override
    public List<Post> findByBoardType(BoardType boardType, PostSortType sort,
                                      String keyword, int page, int size) {
        if (sort == PostSortType.comments) {
            return em.createQuery("""
                SELECT p FROM PostJpaEntity p
                WHERE p.boardType = :boardType
                  AND p.title LIKE :keyword
                  AND p.status = :status
                ORDER BY (
                    SELECT COUNT(c) FROM CommentJpaEntity c WHERE c.postId = p.id
                ) DESC
                """, PostJpaEntity.class)
                    .setParameter("boardType", boardType)
                    .setParameter("keyword", "%" + (keyword != null ? keyword : "") + "%")
                    .setParameter("status", PostStatus.ACTIVE)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList()
                    .stream().map(this::toDomain).toList();
        }
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        return repository.findByBoardTypeAndTitleContainingAndStatus(
                        boardType, keyword != null ? keyword : "", PostStatus.ACTIVE, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Post> findAll(PostSortType sort, String keyword, int page, int size) {
        if (sort == PostSortType.comments) {
            return em.createQuery("""
                SELECT p FROM PostJpaEntity p
                WHERE p.title LIKE :keyword
                  AND p.status = :status
                ORDER BY (
                    SELECT COUNT(c) FROM CommentJpaEntity c WHERE c.postId = p.id
                ) DESC
                """, PostJpaEntity.class)
                    .setParameter("keyword", "%" + (keyword != null ? keyword : "") + "%")
                    .setParameter("status", PostStatus.ACTIVE)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList()
                    .stream().map(this::toDomain).toList();
        }
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        return repository.findByTitleContainingAndStatus(
                        keyword != null ? keyword : "", PostStatus.ACTIVE, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countByBoardType(BoardType boardType, String keyword) {
        return repository.countByBoardTypeAndTitleContainingAndStatus(
                boardType, keyword != null ? keyword : "", PostStatus.ACTIVE);
    }

    @Override
    public int countAll(String keyword) {
        return repository.countByTitleContainingAndStatus(
                keyword != null ? keyword : "", PostStatus.ACTIVE);
    }

    private Sort toSort(PostSortType sort) {
        return switch (sort) {
            case views -> Sort.by(Sort.Direction.DESC, "viewCount");
            case comments -> Sort.by(Sort.Direction.DESC, "createdAt");
            case latest -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Post toDomain(PostJpaEntity entity) {
        return Post.restore(
                entity.getId(), entity.getAuthorId(), entity.getBoardType(),
                entity.getSubject(),
                entity.getTitle(), entity.getContent(),
                entity.getViewCount(), entity.getStatus(), entity.isAccepted(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }


    @Override
    public Optional<Post> findById(Long postId) {
        return repository.findById(postId).map(this::toDomain);
    }

    @Override
    public void updateViewCount(Long postId, int viewCount) {
        repository.findById(postId).ifPresent(entity -> {
            entity.updateViewCount(viewCount);
            repository.save(entity);
        });
    }

    @Override
    public void deleteById(Long postId) {
        repository.deleteById(postId);
    }

    @Override
    public void adminDeleteById(Long postId) {
        PostJpaEntity entity = repository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        Post post = toDomain(entity);
        post.softDeleteByAdmin(LocalDateTime.now());

        entity.update(
                post.getSubject(), post.getTitle(), post.getContent(),
                post.getViewCount(), post.getStatus(), post.isAccepted(), post.getUpdatedAt()
        );
        repository.save(entity);
    }

}
