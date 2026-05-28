package com.wanted.backend.domain.community.presentation.response;

public record FileUploadResponse(
        Long fileId,
        String fileUrl
) {}