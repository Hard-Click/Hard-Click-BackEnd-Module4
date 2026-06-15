package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.ChangeMemberStatusCommand;
import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;
import com.wanted.backend.domain.identity.application.usecase.ChangeMemberStatusUseCase;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusHistory;
import com.wanted.backend.domain.identity.domain.policy.MemberStatusChangePolicy;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.domain.repository.MemberStatusHistoryRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMemberStatusService implements ChangeMemberStatusUseCase {

    private final MemberRepository memberRepository;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;

    @Override
    public ChangeMemberStatusResult changeStatus(ChangeMemberStatusCommand command) {
        MemberStatus status = parseStatus(command.status());
        validateChangeableStatus(status);

        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberStatus previousStatus = member.getStatus();
        LocalDateTime now = LocalDateTime.now();
        String memo = normalizeMemo(command.memo());

        member.changeCommunityStatus(status, now);
        Member savedMember = memberRepository.save(member);
        memberStatusHistoryRepository.save(MemberStatusHistory.create(
                savedMember.getId(),
                previousStatus,
                savedMember.getStatus(),
                memo,
                now
        ));

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

    private void validateChangeableStatus(MemberStatus status) {
        if (!MemberStatusChangePolicy.isCommunityStatusChangeAllowed(status)) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS_CHANGE);
        }
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }
        return memo.trim();
    }
}
