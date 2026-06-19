package com.wanted.backend.domain.notification.presentation.response;

import com.wanted.backend.domain.notification.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationItemResponse(

        @Schema(description = "알림 고유 ID", example = "55")
        Long notiId,

        @Schema(description = "알림 타입", example = "NOTICE")
        NotificationType type,

        @Schema(description = "알림 내용", example = "새로운 공지사항이 등록되었습니다.")
        String message,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "클릭 시 이동할 URL", example = "/notices/1")
        String redirectUrl,

        @Schema(description = "수신 일시", example = "2026-05-10T16:00:00")
        LocalDateTime createdAt
) {}