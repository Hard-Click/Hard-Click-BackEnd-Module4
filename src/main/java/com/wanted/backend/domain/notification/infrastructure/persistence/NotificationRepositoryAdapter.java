package com.wanted.backend.domain.notification.infrastructure.persistence;

import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository repository;

    public NotificationRepositoryAdapter(SpringDataNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity(
                notification.getReceiverId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getRedirectUrl(),
                notification.getCreatedAt()
        );
        return toDomain(repository.save(entity));
    }

    @Override
    public int countUnreadByReceiverId(Long receiverId) {
        return repository.countByReceiverIdAndIsRead(receiverId, false);
    }

    @Override
    public Optional<Notification> findById(Long notificationId) {
        return repository.findById(notificationId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void updateRead(Long notificationId) {
        NotificationJpaEntity entity = repository.findById(notificationId).orElseThrow();
        entity.markAsRead();
    }

    @Override
    public List<Notification> findByReceiverIdWithCursor(Long receiverId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<NotificationJpaEntity> entities = cursorId == null
                ? repository.findByReceiverIdOrderByIdDesc(receiverId, pageable)
                : repository.findByReceiverIdAndIdLessThanOrderByIdDesc(receiverId, cursorId, pageable);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsNextPage(Long receiverId, Long lastId) {
        return repository.existsByReceiverIdAndIdLessThan(receiverId, lastId);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        List<NotificationJpaEntity> entities = notifications.stream()
                .map(n -> new NotificationJpaEntity(
                        n.getReceiverId(), n.getType(), n.getMessage(),
                        n.isRead(), n.getRedirectUrl(), n.getCreatedAt()))
                .toList();
        return repository.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByRedirectUrlStartingWith(String urlPrefix) {
        repository.deleteByRedirectUrlStartingWith(urlPrefix);
    }

    // redirectUrl "/notices/{noticeId}" 기반으로 단건 읽음 여부 확인
    @Override
    public boolean isNoticeRead(Long memberId, Long noticeId) {
        return repository.existsByReceiverIdAndRedirectUrlAndIsReadTrue(
                memberId, "/notices/" + noticeId);
    }

    // noticeId 목록을 redirectUrl로 변환 후 읽음 처리된 공지 ID만 추출하여 반환
    // noticeId 목록을 redirectUrl로 변환 후 읽음 처리된 공지 ID만 추출하여 반환
    @Override
    public List<Long> findReadNoticeIds(Long memberId, List<Long> noticeIds) {
        List<String> redirectUrls = noticeIds.stream()
                .map(id -> "/notices/" + id)
                .toList();
        List<String> readUrls = repository
                .findByReceiverIdAndRedirectUrlInAndIsReadTrue(memberId, redirectUrls)
                .stream()
                .map(NotificationJpaEntity::getRedirectUrl)
                .collect(Collectors.toList());

        // Long.parseLong() 파싱 제거 — 원본 noticeIds에서 역방향 필터링
        return noticeIds.stream()
                .filter(id -> readUrls.contains("/notices/" + id))
                .collect(Collectors.toList());
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return Notification.restore(
                entity.getId(),
                entity.getReceiverId(),
                entity.getType(),
                entity.getMessage(),
                entity.isRead(),
                entity.getRedirectUrl(),
                entity.getCreatedAt()
        );
    }
}