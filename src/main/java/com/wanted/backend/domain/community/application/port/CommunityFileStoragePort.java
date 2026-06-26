package com.wanted.backend.domain.community.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface CommunityFileStoragePort {

    String store(MultipartFile file, String prefix, long maxFileSize);

    /** 저장된 S3 key를 조회용 Presigned URL로 변환한다. */
    String presignUrl(String key);

    void delete(String fileKey);
}
