package com.wanted.backend.domain.notice.application.command;


public record GetNoticeListCommand(
        String type,
        Long courseId,
        String keyword,
        int page,
        int size,
        Long memberId,
        String role
) {

}