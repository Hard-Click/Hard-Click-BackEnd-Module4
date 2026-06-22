package com.wanted.backend.domain.report_moderation.application.dto;

import com.wanted.backend.domain.community.domain.model.TargetType;

import java.time.LocalDateTime;

public record AdminContentResult(
        TargetType contentType,
        Long contentId,
        String title,
        String content,
        String status,
        Long authorMemberId,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
