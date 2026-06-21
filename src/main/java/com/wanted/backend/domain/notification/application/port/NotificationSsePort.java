package com.wanted.backend.domain.notification.application.port;

import com.wanted.backend.domain.notification.presentation.response.NotificationItemResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결 관리 포트.
 * 어댑터(NotificationSseAdapter)가 구현한다.
 */
public interface NotificationSsePort {

    /** 회원의 SSE 연결을 등록하고 Emitter를 반환한다 */
    SseEmitter connect(Long memberId);

    /** 특정 회원에게 알림을 SSE로 발송한다 */
    void send(Long memberId, NotificationItemResponse response);

}