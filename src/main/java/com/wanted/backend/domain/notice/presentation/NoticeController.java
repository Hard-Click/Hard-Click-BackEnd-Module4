package com.wanted.backend.domain.notice.presentation;

import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.presentation.request.CreateNoticeRequest;
import com.wanted.backend.domain.notice.presentation.response.CreateNoticeResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/courses/{courseId}/notices")
public class NoticeController {

    private final NoticeCommandUseCase noticeCommandUseCase;

    public NoticeController(NoticeCommandUseCase noticeCommandUseCase) {
        this.noticeCommandUseCase = noticeCommandUseCase;
    }


    @PostMapping
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
}