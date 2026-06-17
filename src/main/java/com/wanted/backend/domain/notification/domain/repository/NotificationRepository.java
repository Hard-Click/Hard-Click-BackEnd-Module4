package com.wanted.backend.domain.notification.domain.repository;

import com.wanted.backend.domain.notification.domain.model.Notification;

public interface NotificationRepository {
    Notification save(Notification notification);
    int countUnreadByReceiverId(Long receiverId);
}