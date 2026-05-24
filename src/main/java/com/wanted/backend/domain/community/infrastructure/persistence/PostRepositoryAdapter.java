package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository repository;

    public PostRepositoryAdapter(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = new PostJpaEntity(
                post.getAuthorId(),
                post.getBoardType(),
                post.getSubjectId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.isAccepted(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
    }

    private Post toDomain(PostJpaEntity entity) {
        return Post.restore(
                entity.getId(),
                entity.getAuthorId(),
                entity.getBoardType(),
                entity.getSubjectId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getViewCount(),
                entity.isAccepted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}