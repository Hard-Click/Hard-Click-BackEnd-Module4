package com.wanted.backend.domain.identity.application.command;

import org.springframework.web.multipart.MultipartFile;

public record UpdateMyProfileCommand(
        Long memberId,
        MultipartFile profileImage,
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {
}
