package com.wanted.backend.domain.notification.presentation;

import com.wanted.backend.domain.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;
import com.wanted.backend.domain.notification.presentation.response.NotificationReadResponse;
import com.wanted.backend.domain.notification.presentation.response.UnreadCountResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 회원의 알림 목록을 커서 기반으로 반환합니다. 첫 요청 시 cursorId 생략."
    )
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId) {

        NotificationListResponse response = notificationQueryUseCase.getList(
                userDetails.getMemberId(), cursorId);

        return ApiResponse.success("알림 목록 조회 완료", response);
    }

    @PatchMapping("/{notiId}/read")
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notiId) {
        NotificationReadResponse response = notificationQueryUseCase.markAsRead(
                userDetails.getMemberId(), notiId);
        return ApiResponse.success("알림 읽음 처리 완료", response);
    }
}