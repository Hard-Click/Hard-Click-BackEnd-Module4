package com.wanted.backend.domain.notification.application.usecase;

import com.wanted.backend.domain.notification.application.result.NotificationListResult;
import com.wanted.backend.domain.notification.application.result.NotificationReadResult;

public interface NotificationQueryUseCase {
    int getUnreadCount(Long memberId);
    NotificationListResult getList(Long memberId, Long cursorId);
    NotificationReadResult markAsRead(Long memberId, Long notificationId);
}