package com.wanted.backend.domain.notice.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateNoticeRequest(

        @Schema(description = "공지사항 제목 (200자 이하)", example = "3주차 강의 업로드 안내 (수정)")
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "공지사항 내용", example = "3주차 강의 업로드 일정이 변경되었습니다. 양해 부탁드립니다.")
        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,

        @Schema(description = "상단 고정 여부", example = "true")
        @NotNull(message = "상단 고정 여부는 필수입니다.")
        Boolean isPinned
) {}