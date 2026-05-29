package com.wanted.backend.domain.identity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "내 프로필 수정 요청")
public record UpdateMyProfileRequest(
        @Schema(description = "프로필 이미지 파일")
        List<MultipartFile> profileImage,

        @Schema(description = "현재 비밀번호", example = "Password123!")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "NewPassword123!")
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!")
        String newPasswordConfirm
) {
    public boolean hasMultipleProfileImages() {
        return getProfileImageCount() > 1;
    }

    public MultipartFile getSingleProfileImage() {
        List<MultipartFile> files = getNotEmptyProfileImages();
        return files.isEmpty() ? null : files.get(0);
    }

    private int getProfileImageCount() {
        return getNotEmptyProfileImages().size();
    }

    private List<MultipartFile> getNotEmptyProfileImages() {
        if (profileImage == null) {
            return List.of();
        }

        return profileImage.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }
}