package com.wanted.backend.domain.notice.presentation;

import com.wanted.backend.domain.notice.application.command.*;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.application.usecase.NoticeQueryUseCase;
import com.wanted.backend.domain.notice.presentation.request.CreateGlobalNoticeRequest;
import com.wanted.backend.domain.notice.presentation.request.CreateNoticeRequest;
import com.wanted.backend.domain.notice.presentation.request.UpdateNoticeRequest;
import com.wanted.backend.domain.notice.presentation.response.CreateNoticeResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeDetailResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeListResponse;
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
    private final NoticeQueryUseCase noticeQueryUseCase;

    public NoticeController(NoticeCommandUseCase noticeCommandUseCase, NoticeQueryUseCase noticeQueryUseCase) {
        this.noticeCommandUseCase = noticeCommandUseCase;
        this.noticeQueryUseCase = noticeQueryUseCase;
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

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<NoticeListResponse>> getNotices(
            @RequestParam String type,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        NoticeListResponse response = noticeQueryUseCase.getList(
                new GetNoticeListCommand(type, courseId, keyword, page, size));

        return ApiResponse.success("공지사항 목록 조회 성공", response);
    }

    @GetMapping("/notices/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(
            @PathVariable Long noticeId) {

        NoticeDetailResponse response = noticeQueryUseCase.getDetail(noticeId);

        return ApiResponse.success("공지사항 상세 조회 성공", response);
    }

    @PatchMapping("/notices/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest request) {

        noticeCommandUseCase.update(new UpdateNoticeCommand(
                userDetails.getMemberId(),
                noticeId,
                request.title(),
                request.content(),
                request.isPinned()
        ));

        return ApiResponse.successNoContent("공지사항이 수정되었습니다.");
    }

    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noticeId) {

        noticeCommandUseCase.delete(new DeleteNoticeCommand(
                userDetails.getMemberId(),
                noticeId
        ));

        return ApiResponse.successNoContent("공지사항이 삭제되었습니다.");
    }
}