package com.wanted.backend.domain.identity.presentation.api;

import com.wanted.backend.domain.identity.application.usecase.MemberStatusStreamUseCase;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member Status Stream", description = "회원 상태 실시간 이벤트 API")
public class MemberStatusStreamController {

    private final MemberStatusStreamUseCase memberStatusStreamUseCase;

    @GetMapping(value = "/me/status-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "내 회원 상태 실시간 조회",
            description = "회원 상태 변경 시 MEMBER_STATUS_CHANGED SSE 이벤트를 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공 (text/event-stream)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<SseEmitter> connect(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SseEmitter emitter = memberStatusStreamUseCase.connect(userDetails.getMemberId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .cacheControl(CacheControl.noCache())
                .body(emitter);
    }
}
