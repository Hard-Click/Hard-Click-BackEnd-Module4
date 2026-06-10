package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FileUploadResponse(

        @Schema(description = "업로드된 파일 ID", example = "15")
        Long fileId,

        @Schema(description = "업로드된 파일 접근 URL", example = "http://localhost:8080/files/post/abc123.jpg")
        String fileUrl
) {

}