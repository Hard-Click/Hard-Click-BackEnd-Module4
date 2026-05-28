package com.wanted.backend.domain.community.application.command;

import org.springframework.web.multipart.MultipartFile;

public record FileUploadCommand(
        Long uploaderId,
        MultipartFile file,
        String fileType
) {}