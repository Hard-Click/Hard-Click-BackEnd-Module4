package com.wanted.backend.domain.notification.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnreadCountResponse(

        @Schema(description = "읽지 않은 알림 총 개수", example = "3")
        int unreadCount
) {
        public static UnreadCountResponse from(int count) {
                return new UnreadCountResponse(count);
        }
}