package com.wanted.backend.domain.notification.infrastructure.persistence;

import com.wanted.backend.domain.notification.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification",
        indexes = @Index(
                name = "idx_notification_receiver_read_url",
                columnList = "receiver_id, is_read, redirect_url"
        ))
@Getter
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NotificationJpaEntity() {}

    public NotificationJpaEntity(Long receiverId, NotificationType type, String message,
                                 boolean isRead, String redirectUrl, LocalDateTime createdAt) {
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.redirectUrl = redirectUrl;
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}