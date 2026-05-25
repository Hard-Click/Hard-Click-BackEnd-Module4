package com.wanted.backend.domain.community.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UpdatePostRequest(

        Long subjectId,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 300, message = "제목은 300자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {

}