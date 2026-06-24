package com.wanted.backend.domain.identity.application.listener;

import com.wanted.backend.domain.community.domain.event.MemberSuspendedEvent;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSuspendEventListener {

    private final MemberRepository memberRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMemberSuspended(MemberSuspendedEvent event) {
        Member member = memberRepository.findById(event.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.SUSPENDED
                || member.getStatus() == MemberStatus.WITHDRAWN) {
            return;
        }

        member.changeCommunityStatus(MemberStatus.SUSPENDED, LocalDateTime.now());
        memberRepository.save(member);

        log.info("[AutoSuspend] memberId: {} — 50명 distinct 신고 누적으로 커뮤니티 제한", event.memberId());
    }
}