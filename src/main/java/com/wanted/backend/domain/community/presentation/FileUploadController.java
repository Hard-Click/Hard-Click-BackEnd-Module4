package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.FileUploadCommand;
import com.wanted.backend.domain.community.application.usecase.FileUploadUseCase;
import com.wanted.backend.domain.community.presentation.response.FileUploadResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * [Presentation Layer - Controller]
 *
 * POST /api/files/post    게시글용 이미지 업로드
 * POST /api/files/comment 댓글용 이미지 업로드
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadUseCase fileUploadUseCase;

    public FileUploadController(FileUploadUseCase fileUploadUseCase) {
        this.fileUploadUseCase = fileUploadUseCase;
    }

    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadPostImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,  // JWT에서 추출
            @RequestPart("file") MultipartFile file) {

        FileUploadResponse response = fileUploadUseCase.handle(
                new FileUploadCommand(userDetails.getMemberId(), file, "POST"));

        return ApiResponse.created("파일이 업로드되었습니다.", response);
    }

    @PostMapping(value = "/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadCommentImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {

        FileUploadResponse response = fileUploadUseCase.handle(
                new FileUploadCommand(userDetails.getMemberId(), file, "COMMENT"));

        return ApiResponse.created("파일이 업로드되었습니다.", response);
    }
}