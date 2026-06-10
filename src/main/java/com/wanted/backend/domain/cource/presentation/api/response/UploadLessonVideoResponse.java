package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "레슨 영상 업로드 응답")
public record UploadLessonVideoResponse(
        @Schema(description = "레슨 ID", example = "3")
        Long lessonId,

        @Schema(description = "업로드된 영상 URL", example = "http://localhost:8080/uploads/videos/lesson_3_abc.mp4")
        String videoUrl,

        @Schema(description = "파일 처리 상태 (업로드 직후 항상 PENDING)", example = "PENDING")
        FileProcessingStatus fileProcessingStatus
) {}
