package com.wanted.backend.domain.notification.presentation.response;

import com.wanted.backend.domain.notification.application.result.NotificationReadResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 읽음 처리 응답")
public record NotificationReadResponse(
        @Schema(description = "읽음 처리된 알림 ID", example = "55")
        Long notiId,
        @Schema(description = "읽음 여부", example = "true")
        boolean isRead
) {

    public static NotificationReadResponse from(NotificationReadResult result) {
        return new NotificationReadResponse(result.notiId(), result.isRead());
    }
}