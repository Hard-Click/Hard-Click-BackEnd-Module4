package com.wanted.backend.domain.community.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UpdateCommentRequest(

        @Schema(description = "댓글 내용 (300자 이하)", example = "내용을 보완하자면 추가적으로 설명드리겠습니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다.")
        String content
) {

}