package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.ViewLog;
import com.wanted.backend.domain.community.domain.repository.ViewLogRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public class ViewLogRepositoryAdapter implements ViewLogRepository {

    private final SpringDataViewLogRepository repository;

    public ViewLogRepositoryAdapter(SpringDataViewLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public ViewLog save(ViewLog viewLog) {
        ViewLogJpaEntity entity = new ViewLogJpaEntity(
                viewLog.getMemberId(),
                viewLog.getPostId(),
                viewLog.getViewedAt()
        );
        ViewLogJpaEntity saved = repository.save(entity);


        return ViewLog.restore(saved.getId(), saved.getMemberId(),
                saved.getPostId(), saved.getViewedAt());
    }

    @Override
    public boolean existsByMemberIdAndPostIdAndViewedAtAfter(
            Long memberId, Long postId, LocalDateTime after) {
        return repository.existsByMemberIdAndPostIdAndViewedAtAfter(
                memberId, postId, after);
    }
}