package com.wanted.backend.domain.community.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateCommentRequest(

        @Schema(description = "게시글 ID", example = "37")
        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId,

        @Schema(description = "부모 댓글 ID (대댓글일 경우 필수)", example = "12")
        Long parentId,

        @Schema(description = "댓글 내용 (300자 이하)", example = "좋은 질문입니다. JWT 필터는 OncePerRequestFilter를 상속받아 구현합니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 300, message = "댓글 내용은 300자 이하여야 합니다.")
        String content
) {}