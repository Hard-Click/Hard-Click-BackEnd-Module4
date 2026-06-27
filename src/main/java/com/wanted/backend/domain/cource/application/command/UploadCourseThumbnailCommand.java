package com.wanted.backend.domain.cource.application.command;

public record UploadCourseThumbnailCommand(
        Long courseId,
        Long requesterId,
        String originalFilename,
        byte[] imageData
) {}
