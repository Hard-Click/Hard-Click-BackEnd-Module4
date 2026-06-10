package com.wanted.backend.domain.identity.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStoragePort {

    String store(MultipartFile file);
}
