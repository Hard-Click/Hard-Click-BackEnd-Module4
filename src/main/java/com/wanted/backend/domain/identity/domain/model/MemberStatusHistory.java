package com.wanted.backend.domain.identity.domain.model;

import java.time.LocalDateTime;

public class MemberStatusHistory {

    private final Long id;
    private final Long memberId;
    private final MemberStatus previousStatus;
    private final MemberStatus changedStatus;
    private final String memo;
    private final LocalDateTime createdAt;

    private MemberStatusHistory(
            Long id,
            Long memberId,
            MemberStatus previousStatus,
            MemberStatus changedStatus,
            String memo,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.previousStatus = previousStatus;
        this.changedStatus = changedStatus;
        this.memo = memo;
        this.createdAt = createdAt;
    }

    public static MemberStatusHistory create(
            Long memberId,
            MemberStatus previousStatus,
            MemberStatus changedStatus,
            String memo,
            LocalDateTime createdAt
    ) {
        return new MemberStatusHistory(null, memberId, previousStatus, changedStatus, memo, createdAt);
    }

    public static MemberStatusHistory restore(
            Long id,
            Long memberId,
            MemberStatus previousStatus,
            MemberStatus changedStatus,
            String memo,
            LocalDateTime createdAt
    ) {
        return new MemberStatusHistory(id, memberId, previousStatus, changedStatus, memo, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public MemberStatus getPreviousStatus() {
        return previousStatus;
    }

    public MemberStatus getChangedStatus() {
        return changedStatus;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
