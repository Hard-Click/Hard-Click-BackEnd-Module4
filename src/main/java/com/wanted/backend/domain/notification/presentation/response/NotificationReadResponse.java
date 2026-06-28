package com.wanted.backend.domain.notification.presentation.response;

import com.wanted.backend.domain.notification.application.result.NotificationReadResult;

public record NotificationReadResponse(Long notiId, boolean isRead) {

    public static NotificationReadResponse from(NotificationReadResult result) {
        return new NotificationReadResponse(result.notiId(), result.isRead());
    }
}