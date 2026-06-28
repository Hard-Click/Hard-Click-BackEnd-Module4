package com.wanted.backend.domain.notification.application.result;

import java.util.List;

public record NotificationListResult(
        List<NotificationItemResult> content,
        boolean hasNext
) {}