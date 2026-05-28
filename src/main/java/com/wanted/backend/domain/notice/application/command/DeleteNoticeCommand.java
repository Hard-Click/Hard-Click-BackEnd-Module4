package com.wanted.backend.domain.notice.application.command;


public record DeleteNoticeCommand(
        Long memberId,
        Long noticeId
) {

}