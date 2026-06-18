package com.wanted.backend.domain.community.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface CommunityFileStoragePort {

    String store(MultipartFile file, String prefix, long maxFileSize);

    void delete(String fileUrl);
}
