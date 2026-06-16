package com.wanted.backend.domain.payment.application.command;

import java.util.List;

public record CreateCourseOrderCommand(
        Long memberId,
        List<Long> courseIds
) {}
