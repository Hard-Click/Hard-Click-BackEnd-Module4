package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.dto.MemberStatusChangedMessage;
import com.wanted.backend.domain.identity.application.dto.MemberStatusSyncMessage;
import com.wanted.backend.domain.identity.application.port.MemberStatusStreamPort;
import com.wanted.backend.domain.identity.application.usecase.MemberStatusStreamUseCase;
import com.wanted.backend.domain.identity.domain.event.MemberStatusChangedEvent;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberStatusStreamService implements MemberStatusStreamUseCase {

    private final MemberRepository memberRepository;
    private final MemberStatusStreamPort memberStatusStreamPort;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public SseEmitter connect(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberStatusSyncMessage syncMessage = MemberStatusSyncMessage.from(member, LocalDateTime.now(clock));

        return memberStatusStreamPort.connect(memberId, syncMessage);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberStatusChangedEvent event) {
        memberStatusStreamPort.send(MemberStatusChangedMessage.from(event));
    }
}
