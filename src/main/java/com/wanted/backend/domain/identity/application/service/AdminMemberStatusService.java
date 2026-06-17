package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.ChangeMemberStatusCommand;
import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;
import com.wanted.backend.domain.identity.application.usecase.ChangeMemberStatusUseCase;
import com.wanted.backend.domain.identity.domain.event.MemberStatusChangedEvent;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusChangeReason;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMemberStatusService implements ChangeMemberStatusUseCase {

    private final MemberRepository memberRepository;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ChangeMemberStatusResult changeStatus(ChangeMemberStatusCommand command) {
        MemberStatus status = parseStatus(command.status());

        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberStatus previousStatus = member.getStatus();
        LocalDateTime now = LocalDateTime.now(clock);
        String memo = normalizeMemo(command.memo());

        changeCommunityStatus(member, status, now);
        Member savedMember = memberRepository.save(member);
        publishStatusChangedEvent(savedMember, previousStatus, now);

        return new ChangeMemberStatusResult(
                savedMember.getId(),
                savedMember.getStatus(),
                memo
        );
    }

    private MemberStatus parseStatus(String status) {
        try {
            return MemberStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS_CHANGE);
        }
    }

    private void changeCommunityStatus(Member member, MemberStatus status, LocalDateTime now) {
        try {
            member.changeCommunityStatus(status, now);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS_CHANGE);
        }
    }

    private void publishStatusChangedEvent(Member member, MemberStatus previousStatus, LocalDateTime now) {
        if (previousStatus == member.getStatus()) {
            return;
        }

        eventPublisher.publishEvent(new MemberStatusChangedEvent(
                member.getId(),
                previousStatus,
                member.getStatus(),
                MemberStatusChangeReason.ADMIN_MANUAL,
                statusChangedMessage(member.getStatus()),
                now
        ));
    }

    private String statusChangedMessage(MemberStatus status) {
        if (status == MemberStatus.SUSPENDED) {
            return "관리자에 의해 커뮤니티 작성이 제한되었습니다.";
        }
        return "커뮤니티 작성 제한이 해제되었습니다.";
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.trim();
    }
}
