package com.wanted.backend.domain.identity.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStoragePort {

    String store(MultipartFile file);

    /** 저장된 S3 key를 조회용 Presigned URL로 변환한다. */
    String presignUrl(String key);
}
