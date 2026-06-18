package com.wanted.backend.domain.notification.application.usecase;

public interface NotificationQueryUseCase {
    int getUnreadCount(Long memberId);
}