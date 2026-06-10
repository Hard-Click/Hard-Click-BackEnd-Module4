package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.FileUploadCommand;
import com.wanted.backend.domain.community.presentation.response.FileUploadResponse;

public interface FileUploadUseCase {
    FileUploadResponse handle(FileUploadCommand command);
}