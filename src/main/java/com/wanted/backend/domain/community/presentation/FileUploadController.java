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


@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadUseCase fileUploadUseCase;

    public FileUploadController(FileUploadUseCase fileUploadUseCase) {
        this.fileUploadUseCase = fileUploadUseCase;
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("fileType") String fileType,
            @RequestPart("file") MultipartFile file) {

        FileUploadResponse response = fileUploadUseCase.handle(
                new FileUploadCommand(userDetails.getMemberId(), file, fileType));

        return ApiResponse.created("파일이 업로드되었습니다.", response);
    }
}