package com.wanted.backend.domain.notice.application.command;


public record CreateNoticeCommand(
        Long instructorId,
        Long courseId,
        String title,
        String content,
        Boolean isPinned
) {}