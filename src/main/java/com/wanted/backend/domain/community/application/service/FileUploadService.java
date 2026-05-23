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

    // application.yaml의 community BC 전용 설정값 주입
    @Value("${community.image.post-dir}")
    private String postDir;         // 게시글 이미지 저장 경로

    @Value("${community.image.comment-dir}")
    private String commentDir;      // 댓글 이미지 저장 경로

    @Value("${community.image.post-url}")
    private String postUrl;         // 게시글 이미지 접근 URL

    @Value("${community.image.comment-url}")
    private String commentUrl;      // 댓글 이미지 접근 URL

    @Value("${community.image.max-size}")
    private long maxFileSize;       // community BC 전용 파일 크기 제한 (5MB)

    private final UploadedFileRepository uploadedFileRepository;

    public FileUploadService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
    }

    @Override
    public FileUploadResponse handle(FileUploadCommand command) {

        // fileType(POST/COMMENT)에 따라 저장 경로와 URL 분기
        String uploadDir = command.fileType().equals("POST") ? postDir : commentDir;
        String baseUrl = command.fileType().equals("POST") ? postUrl : commentUrl;

        String savedFileName = null;

        try {
            // 1. 로컬 디스크에 파일 저장 (형식/크기 검증 + UUID 난수화 포함)
            savedFileName = FileUploadUtils.saveFile(command.file(), uploadDir, maxFileSize);

            // 2. 클라이언트가 접근할 전체 URL 생성
            String fileUrl = baseUrl + savedFileName;

            // 3. DB에 파일 정보 저장
            UploadedFile saved = uploadedFileRepository.save(
                    UploadedFile.create(
                            command.uploaderId(),
                            command.file().getOriginalFilename(),  // 원본 파일명
                            fileUrl,
                            command.fileType(),
                            command.file().getSize()
                    )
            );

            return new FileUploadResponse(saved.getId(), saved.getFileUrl());

        } catch (IOException e) {
            // DB 저장 실패 시 디스크에 저장된 파일 롤백 삭제
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(uploadDir, savedFileName);
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}