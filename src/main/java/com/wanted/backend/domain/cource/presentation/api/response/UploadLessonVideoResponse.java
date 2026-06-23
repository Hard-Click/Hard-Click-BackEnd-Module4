package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "레슨 영상 업로드 응답")
public record UploadLessonVideoResponse(
        @Schema(description = "레슨 ID", example = "3")
        Long lessonId,

        @Schema(description = "업로드된 영상 재생 URL (S3 Presigned, 7일 만료)",
                example = "https://hard-click-bucket.s3.ap-northeast-2.amazonaws.com/videos/3_abc.mp4?X-Amz-Algorithm=...")
        String videoUrl,

        @Schema(description = "파일 처리 상태 (업로드 직후 항상 PENDING)", example = "PENDING")
        FileProcessingStatus fileProcessingStatus
) {}
