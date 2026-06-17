package com.wanted.backend.domain.identity.application.port;

import com.wanted.backend.domain.identity.application.dto.MemberStatusChangedMessage;
import com.wanted.backend.domain.identity.application.dto.MemberStatusSyncMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MemberStatusStreamPort {
    SseEmitter connect(Long memberId, MemberStatusSyncMessage syncMessage);

    void send(MemberStatusChangedMessage message);

    void sendHeartbeat();
}
