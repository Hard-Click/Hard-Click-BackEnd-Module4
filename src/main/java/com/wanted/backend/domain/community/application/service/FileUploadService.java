package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.FileUploadCommand;
import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.usecase.FileUploadUseCase;
import com.wanted.backend.domain.community.domain.model.UploadedFile;
import com.wanted.backend.domain.community.domain.repository.UploadedFileRepository;
import com.wanted.backend.domain.community.presentation.response.FileUploadResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileUploadService implements FileUploadUseCase {

    @Value("${community.image.max-size}")
    private long maxFileSize;

    private final CommunityFileStoragePort storagePort;
    private final UploadedFileRepository uploadedFileRepository;
    private final CommunityAccessPolicy communityAccessPolicy;

    public FileUploadService(CommunityFileStoragePort storagePort,
                             UploadedFileRepository uploadedFileRepository,
                             CommunityAccessPolicy communityAccessPolicy) {
        this.storagePort = storagePort;
        this.uploadedFileRepository = uploadedFileRepository;
        this.communityAccessPolicy = communityAccessPolicy;
    }

    @Override
    public FileUploadResponse handle(FileUploadCommand command) {
        communityAccessPolicy.validateAccess(command.uploaderId());

        String prefix = "POST".equals(command.fileType()) ? "posts" : "comments";
        String fileUrl = storagePort.store(command.file(), prefix, maxFileSize);

        try {
            UploadedFile saved = uploadedFileRepository.save(
                    UploadedFile.create(
                            command.uploaderId(),
                            command.file().getOriginalFilename(),
                            fileUrl,
                            command.fileType(),
                            command.file().getSize()
                    )
            );
            return new FileUploadResponse(saved.getId(), saved.getFileUrl());
        } catch (Exception e) {
            storagePort.delete(fileUrl);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }
}
