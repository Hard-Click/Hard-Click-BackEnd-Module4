package com.wanted.backend.domain.notice.application.command;


public record UpdateNoticeCommand(
        Long memberId,
        Long noticeId,
        String title,
        String content,
        Boolean isPinned
) {

}