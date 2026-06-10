package com.wanted.backend.domain.notice.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateNoticeRequest(

        @Schema(description = "공지사항 제목 (200자 이하)", example = "3주차 강의 업로드 안내")
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "공지사항 내용", example = "3주차 강의가 업로드되었습니다. 수강 후 질문은 게시판을 이용해 주세요.")
        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,

        @Schema(description = "상단 고정 여부", example = "false")
        @NotNull(message = "상단 고정 여부는 필수입니다.")
        Boolean isPinned

) {

}