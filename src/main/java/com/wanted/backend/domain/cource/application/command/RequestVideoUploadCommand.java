package com.wanted.backend.domain.cource.application.command;

public record RequestVideoUploadCommand(Long lessonId, Long requesterId, String originalFilename) {}
