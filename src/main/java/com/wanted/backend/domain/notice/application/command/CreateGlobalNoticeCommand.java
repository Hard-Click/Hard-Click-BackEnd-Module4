package com.wanted.backend.domain.notice.application.command;

public record CreateGlobalNoticeCommand(
        Long adminId,
        String title,
        String content,
        Boolean isPinned
) {

}