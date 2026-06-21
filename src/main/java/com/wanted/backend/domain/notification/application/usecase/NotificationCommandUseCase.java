package com.wanted.backend.domain.notification.application.usecase;

import com.wanted.backend.domain.notification.domain.model.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 알림 생성/발송 유스케이스.
 * SSE 구독과 알림 저장+발송 두 가지 기능을 담당한다.
 */
public interface NotificationCommandUseCase {

    /** 회원의 SSE 구독 연결. 연결 즉시 더미 이벤트를 발송해 연결을 확립한다 */
    SseEmitter subscribe(Long memberId);

    /**
     * 알림을 DB에 저장하고 수신자에게 SSE로 실시간 발송한다.
     *
     * @param receiverId  수신자 회원 ID
     * @param type        알림 타입
     * @param message     알림 내용
     * @param redirectUrl 클릭 시 이동할 URL (nullable)
     */
    void send(Long receiverId, NotificationType type, String message, String redirectUrl);
}