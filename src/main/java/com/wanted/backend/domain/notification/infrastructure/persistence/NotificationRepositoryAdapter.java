package com.wanted.backend.domain.notification.infrastructure.persistence;

import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Repository;

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