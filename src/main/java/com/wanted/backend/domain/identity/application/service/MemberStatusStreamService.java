package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.dto.MemberStatusChangedMessage;
import com.wanted.backend.domain.identity.application.port.MemberStatusStreamPort;
import com.wanted.backend.domain.identity.application.usecase.MemberStatusStreamUseCase;
import com.wanted.backend.domain.identity.domain.event.MemberStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class MemberStatusStreamService implements MemberStatusStreamUseCase {

    private final MemberStatusStreamPort memberStatusStreamPort;

    @Override
    public SseEmitter connect(Long memberId) {
        return memberStatusStreamPort.connect(memberId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(MemberStatusChangedEvent event) {
        memberStatusStreamPort.send(MemberStatusChangedMessage.from(event));
    }
}
