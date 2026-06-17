package com.wanted.backend.domain.notification.presentation;

import com.wanted.backend.domain.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.backend.domain.notification.presentation.response.UnreadCountResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;

    public NotificationController(NotificationQueryUseCase notificationQueryUseCase) {
        this.notificationQueryUseCase = notificationQueryUseCase;
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "미확인 알림 개수 조회",
            description = "로그인한 회원의 읽지 않은 알림 개수를 반환합니다."
    )
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int count = notificationQueryUseCase.getUnreadCount(userDetails.getMemberId());
        return ApiResponse.success("미확인 알림 개수 조회 완료", new UnreadCountResponse(count));
    }
}