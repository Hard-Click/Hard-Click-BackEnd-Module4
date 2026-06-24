package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.community.application.port.MemberAutoSuspendPort;
import com.wanted.backend.domain.identity.domain.event.MemberStatusChangedEvent;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusChangeReason;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@Transactional
public class MemberAutoSuspendAdapter implements MemberAutoSuspendPort {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public MemberAutoSuspendAdapter(MemberRepository memberRepository,
                                    ApplicationEventPublisher eventPublisher,
                                    Clock clock) {
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    public void suspendForReportThreshold(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            return;
        }

        MemberStatus previousStatus = member.getStatus();
        LocalDateTime now = LocalDateTime.now(clock);
        member.changeCommunityStatus(MemberStatus.SUSPENDED, now);
        Member savedMember = memberRepository.save(member);

        eventPublisher.publishEvent(new MemberStatusChangedEvent(
                savedMember.getId(),
                previousStatus,
                savedMember.getStatus(),
                MemberStatusChangeReason.REPORT_THRESHOLD,
                "신고 누적으로 커뮤니티 작성이 제한되었습니다.",
                now,
                clock.instant()
        ));
    }
}