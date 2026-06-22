package com.wanted.backend.domain.notification.application.dto;

import com.wanted.backend.domain.notification.domain.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationPayload(
        Long notiId,
        NotificationType type,
        String message,
        boolean isRead,
        String redirectUrl,
        LocalDateTime createdAt
) {}