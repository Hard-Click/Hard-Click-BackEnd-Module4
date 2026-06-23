package com.wanted.backend.domain.report_moderation.application.dto;

import com.wanted.backend.domain.community.domain.model.TargetType;

public record AdminContentStatusResult(
        TargetType contentType,
        Long contentId,
        String status,
        String memo
) {
}
