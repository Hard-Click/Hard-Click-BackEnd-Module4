package com.wanted.backend.domain.cource.application.command;

public record UploadLessonVideoCommand(
        Long lessonId,
        String originalFilename,
        byte[] videoData
) {}
