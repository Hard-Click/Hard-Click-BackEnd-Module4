package com.wanted.backend.domain.notification.application.usecase;

import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;
import com.wanted.backend.domain.notification.presentation.response.NotificationReadResponse;

public interface NotificationQueryUseCase {
    int getUnreadCount(Long memberId);
    NotificationListResponse getList(Long memberId, Long cursorId);
    NotificationReadResponse markAsRead(Long memberId, Long notificationId);
}