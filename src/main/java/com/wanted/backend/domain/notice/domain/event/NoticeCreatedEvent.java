package com.wanted.backend.domain.notice.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

/**
 * 공지사항이 등록됐을 때 발행되는 이벤트.
 *
 * type / createdByAdmin 조합별 알림 대상:
 *   COURSE + createdByAdmin=false (강사 작성) → 관리자 전원 + 해당 강의 수강생
 *   COURSE + createdByAdmin=true  (관리자 작성) → 해당 강의 수강생만
 *   GLOBAL (관리자 전체 공지)                   → 강사 전원 + 학생 전원
 */
public record NoticeCreatedEvent(
        Long noticeId,
        Long courseId,
        String type,
        String title,
        boolean createdByAdmin,
        Instant occurredAt
) implements DomainEvent {

    public static NoticeCreatedEvent of(Long noticeId, Long courseId, String type,
                                        String title, boolean createdByAdmin) {
        return new NoticeCreatedEvent(noticeId, courseId, type, title,
                createdByAdmin, Instant.now());
    }
}