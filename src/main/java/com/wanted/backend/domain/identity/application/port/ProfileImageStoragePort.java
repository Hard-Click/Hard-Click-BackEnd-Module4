package com.wanted.backend.domain.identity.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStoragePort {

    String store(MultipartFile file);

    /** 저장된 S3 key를 조회용 Presigned URL로 변환한다. */
    String presignUrl(String key);

    /** 더 이상 참조되지 않는 프로필 이미지를 S3에서 삭제한다. */
    void delete(String key);
}
