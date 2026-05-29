package com.wanted.backend.domain.notice.infrastructure.persistence;

import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.model.NoticeStatus;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * [Infrastructure Layer - Repository Adapter]
 *
 * Notice(도메인) ↔ NoticeJpaEntity(JPA) 간 변환 담당
 */
@Repository
public class NoticeRepositoryAdapter implements NoticeRepository {

    private final SpringDataNoticeRepository repository;

    public NoticeRepositoryAdapter(SpringDataNoticeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notice save(Notice notice) {
        if (notice.getId() != null) {
            // 수정
            NoticeJpaEntity entity = repository.findById(notice.getId()).orElseThrow();
            entity.update(notice.getTitle(), notice.getContent(),
                    notice.isPinned(), notice.getUpdatedAt());
            return toDomain(repository.save(entity));
        }

        // 신규 저장
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

    @Override
    public void deleteById(Long noticeId) {
        repository.deleteById(noticeId);
    }

    @Override
    public Page<Notice> findGlobalNotices(String keyword, Pageable pageable) {
        return repository.findByTypeAndTitleContaining("GLOBAL", keyword, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Notice> findCourseNotices(Long courseId, String keyword, Pageable pageable) {
        return repository.findByCourseIdAndTypeAndTitleContaining(
                        courseId, "COURSE", keyword, pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<Notice> findById(Long noticeId) {
        return repository.findById(noticeId).map(this::toDomain);
    }

    @Override
    public Optional<Notice> findPreviousNotice(Long noticeId, String type, Long courseId) {
        // courseId 유무에 따라 메서드 분기
        Optional<NoticeJpaEntity> entity = courseId != null
                ? repository.findFirstByIdLessThanAndTypeAndCourseIdOrderByIdDesc(
                noticeId, type, courseId)
                : repository.findFirstByIdLessThanAndTypeOrderByIdDesc(
                noticeId, type);

        return entity.map(this::toDomain);
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