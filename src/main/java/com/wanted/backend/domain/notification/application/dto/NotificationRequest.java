package com.wanted.backend.domain.notification.application.dto;

import com.wanted.backend.domain.notification.domain.model.NotificationType;

public record NotificationRequest(
        Long receiverId,
        NotificationType type,
        String message,
        String redirectUrl
) {}