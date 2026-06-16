package com.wanted.backend.domain.payment.application.command;

public record CourseRefundCommand(
        Long memberId,
        Long courseId,
        String reason
) {}
