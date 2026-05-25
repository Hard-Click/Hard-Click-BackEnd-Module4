package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.FileUploadCommand;
import com.wanted.backend.domain.community.application.usecase.FileUploadUseCase;
import com.wanted.backend.domain.community.domain.model.UploadedFile;
import com.wanted.backend.domain.community.domain.repository.UploadedFileRepository;
import com.wanted.backend.domain.community.infrastructure.file.FileUploadUtils;
import com.wanted.backend.domain.community.presentation.response.FileUploadResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Service
@Transactional
public class FileUploadService implements FileUploadUseCase {

    @Value("${community.image.post-dir}")
    private String postDir;

    @Value("${community.image.comment-dir}")
    private String commentDir;

    @Value("${community.image.post-url}")
    private String postUrl;

    @Value("${community.image.comment-url}")
    private String commentUrl;

    @Value("${community.image.max-size}")
    private long maxFileSize;

    private final UploadedFileRepository uploadedFileRepository;

    public FileUploadService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
    }

    @Override
    public FileUploadResponse handle(FileUploadCommand command) {


        String uploadDir = command.fileType().equals("POST") ? postDir : commentDir;
        String baseUrl = command.fileType().equals("POST") ? postUrl : commentUrl;

        String savedFileName = null;

        try {

            savedFileName = FileUploadUtils.saveFile(command.file(), uploadDir, maxFileSize);


            String fileUrl = baseUrl + savedFileName;


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

        } catch (IOException e) {

            if (savedFileName != null) {
                FileUploadUtils.deleteFile(uploadDir, savedFileName);
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}