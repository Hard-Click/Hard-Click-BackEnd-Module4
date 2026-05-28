package com.wanted.backend.domain.notice.presentation;

import com.wanted.backend.domain.notice.application.command.CreateGlobalNoticeCommand;
import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.presentation.request.CreateGlobalNoticeRequest;
import com.wanted.backend.domain.notice.presentation.request.CreateNoticeRequest;
import com.wanted.backend.domain.notice.presentation.response.CreateNoticeResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class NoticeController {

    private final NoticeCommandUseCase noticeCommandUseCase;

    public NoticeController(NoticeCommandUseCase noticeCommandUseCase) {
        this.noticeCommandUseCase = noticeCommandUseCase;
    }


    @PostMapping("/courses/{courseId}/notices")
    public ResponseEntity<ApiResponse<CreateNoticeResponse>> createNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long courseId,
            @Valid @RequestBody CreateNoticeRequest request) {

        Long noticeId = noticeCommandUseCase.create(new CreateNoticeCommand(
                userDetails.getMemberId(),
                courseId,
                request.title(),
                request.content(),
                request.isPinned()
        ));

        return ApiResponse.created("강의 공지사항 작성 완료", new CreateNoticeResponse(noticeId));
    }


    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<CreateNoticeResponse>> createGlobalNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGlobalNoticeRequest request) {

        Long noticeId = noticeCommandUseCase.createGlobal(new CreateGlobalNoticeCommand(
                userDetails.getMemberId(),
                request.title(),
                request.content(),
                request.isPinned()
        ));

        return ApiResponse.created("전체 공지사항 작성 완료", new CreateNoticeResponse(noticeId));
    }
}