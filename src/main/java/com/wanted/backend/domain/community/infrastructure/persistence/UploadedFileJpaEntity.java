package com.wanted.backend.domain.community.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files")
@Getter
public class UploadedFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UploadedFileJpaEntity() {}

    public UploadedFileJpaEntity(Long uploaderId, String originalName,
                                 String fileUrl, String fileType,
                                 Long fileSize, LocalDateTime createdAt) {
        this.uploaderId = uploaderId;
        this.originalName = originalName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }
}