package com.wanted.backend.domain.cource.application.command;

public record UploadLessonVideoCommand(
        Long lessonId,
        Long requesterId,
        String originalFilename,
        //영상 파일의 알맹이 (데이터 그 자체)
        byte[] videoData
) {}
