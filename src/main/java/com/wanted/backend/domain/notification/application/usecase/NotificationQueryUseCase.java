package com.wanted.backend.domain.notification.application.usecase;

import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;

public interface NotificationQueryUseCase {
    int getUnreadCount(Long memberId);
    NotificationListResponse getList(Long memberId, Long cursorId);
}