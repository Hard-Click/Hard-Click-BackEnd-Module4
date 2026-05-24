package com.wanted.backend.domain.community.presentation.request;

import com.wanted.backend.domain.community.domain.model.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotNull(message = "게시판 타입은 필수입니다.")
        BoardType boardType,

        Long subjectId,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 300, message = "제목은 300자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {}