package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class PostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository repository;

    public PostRepositoryAdapter(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = new PostJpaEntity(
                post.getAuthorId(), post.getBoardType(), post.getSubjectId(),
                post.getTitle(), post.getContent(), post.getViewCount(),
                post.isAccepted(), post.getCreatedAt(), post.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
    }

    @Override
    public List<Post> findByBoardType(BoardType boardType, PostSortType sort,
                                      String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, toSort(sort));
        return repository.findByBoardTypeAndTitleContainingAndStatus(
                        boardType, keyword != null ? keyword : "", "ACTIVE", pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Post> findAll(PostSortType sort, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, toSort(sort));
        return repository.findByTitleContainingAndStatus(
                        keyword != null ? keyword : "", "ACTIVE", pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countByBoardType(BoardType boardType, String keyword) {
        return repository.countByBoardTypeAndTitleContainingAndStatus(
                boardType, keyword != null ? keyword : "", "ACTIVE");
    }

    @Override
    public int countAll(String keyword) {
        return repository.countByTitleContainingAndStatus(
                keyword != null ? keyword : "", "ACTIVE");
    }

    // 정렬 방식 변환
    private Sort toSort(PostSortType sort) {
        return switch (sort) {
            case views -> Sort.by(Sort.Direction.DESC, "viewCount");
            case comments -> Sort.by(Sort.Direction.DESC, "createdAt"); // 추후 댓글수로 변경
            case latest -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Post toDomain(PostJpaEntity entity) {
        return Post.restore(
                entity.getId(), entity.getAuthorId(), entity.getBoardType(),
                entity.getSubjectId(), entity.getTitle(), entity.getContent(),
                entity.getViewCount(), entity.isAccepted(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}