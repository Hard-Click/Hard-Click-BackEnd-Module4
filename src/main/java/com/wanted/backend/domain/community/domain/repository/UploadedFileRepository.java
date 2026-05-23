package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.UploadedFile;

public interface UploadedFileRepository {
    UploadedFile save(UploadedFile uploadedFile);
}