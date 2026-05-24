package com.wanted.backend.domain.identity.presentation.api.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UpdateMyProfileRequest {

    private List<MultipartFile> profileImage;

    private String currentPassword;

    private String newPassword;

    private String newPasswordConfirm;

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
