package com.wanted.backend.domain.notice.infrastructure.persistence;

import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Repository;

@Repository
public class NoticeRepositoryAdapter implements NoticeRepository {

    private final SpringDataNoticeRepository repository;

    public NoticeRepositoryAdapter(SpringDataNoticeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notice save(Notice notice) {
        NoticeJpaEntity entity = new NoticeJpaEntity(
                notice.getAuthorId(),
                notice.getCourseId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.getType(),
                notice.getStatus(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
        return toDomain(repository.save(entity));
    }

    private Notice toDomain(NoticeJpaEntity entity) {
        return Notice.restore(
                entity.getId(),
                entity.getAuthorId(),
                entity.getCourseId(),
                entity.getTitle(),
                entity.getContent(),
                entity.isPinned(),
                entity.getType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}