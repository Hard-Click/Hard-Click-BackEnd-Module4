package com.wanted.backend.domain.notification.application.service;

import com.wanted.backend.domain.notification.application.dto.NotificationPayload;
import com.wanted.backend.domain.notification.application.dto.NotificationRequest;
import com.wanted.backend.domain.notification.application.port.NotificationSsePort;
import com.wanted.backend.domain.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.model.NotificationType;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Service
public class NotificationCommandService implements NotificationCommandUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationSsePort notificationSsePort;

    public NotificationCommandService(NotificationRepository notificationRepository,
                                      NotificationSsePort notificationSsePort) {
        this.notificationRepository = notificationRepository;
        this.notificationSsePort = notificationSsePort;
    }

    @Override
    @Transactional(readOnly = true)  // SSE 연결은 DB 쓰기 없음
    public SseEmitter subscribe(Long memberId) {
        return notificationSsePort.connect(memberId);
    }

    @Override
    @Transactional
    public void sendBatch(List<NotificationRequest> requests) {
        List<Notification> notifications = requests.stream()
                .map(req -> Notification.create(req.receiverId(), req.type(), req.message(), req.redirectUrl()))
                .toList();
        List<Notification> saved = notificationRepository.saveAll(notifications);
        for (Notification noti : saved) {
            notificationSsePort.send(noti.getReceiverId(), new NotificationPayload(
                    noti.getId(), noti.getType(), noti.getMessage(),
                    noti.isRead(), noti.getRedirectUrl(), noti.getCreatedAt()));
        }
    }

    @Override
    @Transactional
    public void send(Long receiverId, NotificationType type, String message, String redirectUrl) {
        // [1] DB 저장
        Notification notification = notificationRepository.save(
                Notification.create(receiverId, type, message, redirectUrl)
        );

        // [2] SSE 실시간 발송 (연결 안 돼 있으면 어댑터에서 조용히 무시)
        notificationSsePort.send(receiverId, new NotificationPayload(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getRedirectUrl(),
                notification.getCreatedAt()
        ));
        log.debug("[Notification] sent to memberId={}, type={}", receiverId, type);
    }
}