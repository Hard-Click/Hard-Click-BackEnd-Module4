package com.wanted.backend.domain.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository
        extends JpaRepository<NotificationJpaEntity, Long> {

    int countByReceiverIdAndIsRead(Long receiverId, boolean isRead);
}