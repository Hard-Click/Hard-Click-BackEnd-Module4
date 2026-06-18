package com.wanted.backend.domain.notification.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationListResponse(

        @Schema(description = "알림 목록")
        List<NotificationItemResponse> content,

        @Schema(description = "다음 알림 존재 여부", example = "true")
        boolean hasNext
) {}