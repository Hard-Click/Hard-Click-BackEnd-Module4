package com.wanted.backend.domain.notification.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class Notification {

    private Long id;
    private Long receiverId;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private String redirectUrl;
    private LocalDateTime createdAt;

    private Notification(Long id, Long receiverId, NotificationType type,
                         String message, boolean isRead, String redirectUrl,
                         LocalDateTime createdAt) {
        this.id = id;
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.redirectUrl = redirectUrl;
        this.createdAt = createdAt;
    }

    public static Notification create(Long receiverId, NotificationType type,
                                      String message, String redirectUrl) {
        if (receiverId == null || type == null || message == null || message.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_NOTIFICATION);
        }
        return new Notification(null, receiverId, type, message, false,
                redirectUrl, LocalDateTime.now());
    }

    public static Notification restore(Long id, Long receiverId, NotificationType type,
                                       String message, boolean isRead, String redirectUrl,
                                       LocalDateTime createdAt) {
        return new Notification(id, receiverId, type, message, isRead,
                redirectUrl, createdAt);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public Long getId() { return id; }
    public Long getReceiverId() { return receiverId; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getRedirectUrl() { return redirectUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}