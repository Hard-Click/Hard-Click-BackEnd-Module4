package com.wanted.backend.domain.notification.application.service;

import com.wanted.backend.domain.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public int getUnreadCount(Long memberId) {
        return notificationRepository.countUnreadByReceiverId(memberId);
    }
}