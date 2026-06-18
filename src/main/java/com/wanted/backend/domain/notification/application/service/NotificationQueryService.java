package com.wanted.backend.domain.notification.application.service;

import com.wanted.backend.domain.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import com.wanted.backend.domain.notification.presentation.response.NotificationItemResponse;
import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public int getUnreadCount(Long memberId) {
        return notificationRepository.countUnreadByReceiverId(memberId);
    }

    @Override
    public NotificationListResponse getList(Long memberId, Long cursorId) {
        List<Notification> notifications = notificationRepository
                .findByReceiverIdWithCursor(memberId, cursorId, PAGE_SIZE);

        boolean hasNext = false;
        if (!notifications.isEmpty()) {
            Long lastId = notifications.get(notifications.size() - 1).getId();
            hasNext = notificationRepository.existsNextPage(memberId, lastId);
        }

        List<NotificationItemResponse> content = notifications.stream()
                .map(n -> new NotificationItemResponse(
                        n.getId(),
                        n.getType(),
                        n.getMessage(),
                        n.isRead(),
                        n.getRedirectUrl(),
                        n.getCreatedAt()
                ))
                .toList();

        return new NotificationListResponse(content, hasNext);
    }
}