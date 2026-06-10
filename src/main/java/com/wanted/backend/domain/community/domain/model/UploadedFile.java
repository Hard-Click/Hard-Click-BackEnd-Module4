package com.wanted.backend.domain.community.domain.model;

import java.time.LocalDateTime;

public class UploadedFile {

    private Long id;
    private Long uploaderId;
    private String originalName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;

    private UploadedFile(Long id, Long uploaderId, String originalName,
                         String fileUrl, String fileType, Long fileSize,
                         LocalDateTime createdAt) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.originalName = originalName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public static UploadedFile create(Long uploaderId, String originalName,
                                      String fileUrl, String fileType, Long fileSize) {
        return new UploadedFile(null, uploaderId, originalName,
                fileUrl, fileType, fileSize, LocalDateTime.now());
    }

    public static UploadedFile restore(Long id, Long uploaderId, String originalName,
                                       String fileUrl, String fileType, Long fileSize,
                                       LocalDateTime createdAt) {
        return new UploadedFile(id, uploaderId, originalName,
                fileUrl, fileType, fileSize, createdAt);
    }

    public Long getId() { return id; }
    public Long getUploaderId() { return uploaderId; }
    public String getOriginalName() { return originalName; }
    public String getFileUrl() { return fileUrl; }
    public String getFileType() { return fileType; }
    public Long getFileSize() { return fileSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}