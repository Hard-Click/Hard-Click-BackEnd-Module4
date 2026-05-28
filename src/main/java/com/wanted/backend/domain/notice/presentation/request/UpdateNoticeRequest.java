package com.wanted.backend.domain.notice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNoticeRequest(

        @NotBlank(message = "공지사항 제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,

        @NotNull(message = "상단 고정 여부는 필수입니다.")
        Boolean isPinned
) {}