package com.wanted.backend.domain.notification.application.result;

import com.wanted.backend.domain.notification.domain.model.NotificationType;
import java.time.LocalDateTime;

public record NotificationItemResult(
        Long notiId,
        NotificationType type,
        String message,
        boolean isRead,
        String redirectUrl,
        LocalDateTime createdAt
) {}