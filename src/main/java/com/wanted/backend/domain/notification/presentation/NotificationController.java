ackage com.wanted.backend.domain.notification.presentation;

import com.wanted.backend.domain.notification.application.result.NotificationListResult;
import com.wanted.backend.domain.notification.application.result.NotificationReadResult;
import com.wanted.backend.domain.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.backend.domain.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;
import com.wanted.backend.domain.notification.presentation.response.NotificationReadResponse;
import com.wanted.backend.domain.notification.presentation.response.UnreadCountResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;
    private final NotificationCommandUseCase notificationCommandUseCase;

    public NotificationController(NotificationQueryUseCase notificationQueryUseCase,
                                  NotificationCommandUseCase notificationCommandUseCase) {
        this.notificationQueryUseCase = notificationQueryUseCase;
        this.notificationCommandUseCase = notificationCommandUseCase;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 SSE 구독",
            description = "실시간 알림 수신을 위한 SSE 연결을 맺습니다. " +
                    "연결 직후 'connect' 이벤트가 오고, 이후 알림 발생 시 'notification' 이벤트가 옵니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 연결 성공 (text/event-stream)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<SseEmitter> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SseEmitter emitter = notificationCommandUseCase.subscribe(userDetails.getMemberId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .cacheControl(CacheControl.noCache())
                .body(emitter);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "미확인 알림 개수 조회",
            description = "로그인한 회원의 읽지 않은 알림 개수를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미확인 알림 개수 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int count = notificationQueryUseCase.getUnreadCount(userDetails.getMemberId());
        return ApiResponse.success("미확인 알림 개수 조회 완료", UnreadCountResponse.from(count));
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회",
            description = "커서 기반으로 반환합니다. 첫 요청 시 cursorId 생략.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "커서 기반 페이징 ID (첫 요청 시 생략)", example = "55")
            @RequestParam(required = false) Long cursorId) {

        NotificationListResult result = notificationQueryUseCase.getList(
                userDetails.getMemberId(), cursorId);
        return ApiResponse.success("알림 목록 조회 완료", NotificationListResponse.from(result));
    }

    @PatchMapping("/{notiId}/read")
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "읽음 처리할 알림 ID", example = "55")
            @PathVariable Long notiId) {

        NotificationReadResult result = notificationQueryUseCase.markAsRead(
                userDetails.getMemberId(), notiId);
        return ApiResponse.success("알림 읽음 처리 완료", NotificationReadResponse.from(result));
    }
}