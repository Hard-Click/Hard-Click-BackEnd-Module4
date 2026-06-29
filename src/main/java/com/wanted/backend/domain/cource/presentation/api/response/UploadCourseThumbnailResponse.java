package com.wanted.backend.domain.cource.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 썸네일 업로드 응답")
public record UploadCourseThumbnailResponse(
        @Schema(description = "강의 ID", example = "1")
        Long courseId,

        @Schema(description = "업로드된 썸네일 URL (S3 Presigned, 7일 만료)",
                example = "https://hard-click-bucket.s3.ap-northeast-2.amazonaws.com/thumbnails/1_abc.jpg?X-Amz-Algorithm=...")
        String thumbnailUrl
) {}
