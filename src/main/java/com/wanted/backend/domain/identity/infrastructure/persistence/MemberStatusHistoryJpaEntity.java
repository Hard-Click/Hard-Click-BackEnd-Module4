package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_status_histories")
public class MemberStatusHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private MemberStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "changed_status", nullable = false, length = 20)
    private MemberStatus changedStatus;

    @Column(length = 50)
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MemberStatusHistoryJpaEntity() {
    }

    static MemberStatusHistoryJpaEntity from(MemberStatusHistory history) {
        MemberStatusHistoryJpaEntity entity = new MemberStatusHistoryJpaEntity();
        entity.memberId = history.getMemberId();
        entity.previousStatus = history.getPreviousStatus();
        entity.changedStatus = history.getChangedStatus();
        entity.memo = history.getMemo();
        entity.createdAt = history.getCreatedAt();
        return entity;
    }

    MemberStatusHistory toDomain() {
        return MemberStatusHistory.restore(
                id,
                memberId,
                previousStatus,
                changedStatus,
                memo,
                createdAt
        );
    }
}
