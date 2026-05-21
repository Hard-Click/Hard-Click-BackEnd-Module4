package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.domain.model.FileProcessingStatus;

public record UploadLessonVideoResponse(
        Long lessonId,
        String videoUrl,
        FileProcessingStatus fileProcessingStatus  // 응답 시점엔 항상 PENDING
) {}
