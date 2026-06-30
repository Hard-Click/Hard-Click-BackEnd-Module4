package com.wanted.backend.domain.cource.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영상 업로드용 presigned URL 응답")
public record PresignedUploadResponse(
        @Schema(description = "레슨 ID", example = "3")
        Long lessonId,

        @Schema(description = "S3에 PUT 요청할 presigned URL (15분 유효)", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/videos/3_uuid.mp4?X-Amz-...")
        String presignedUrl,

        @Schema(description = "업로드 완료 후 confirm 요청에 사용할 S3 키", example = "videos/3_uuid.mp4")
        String s3Key,

        @Schema(description = "PUT 요청 시 Content-Type 헤더에 그대로 사용해야 하는 값 (서명에 포함됨)", example = "video/mp4")
        String contentType
) {}
