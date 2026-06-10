package com.wanted.backend.domain.community.presentation.request;

import com.wanted.backend.domain.community.domain.model.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @Schema(description = "게시판 타입 (FREE: 자유게시판, QUESTION: 질문게시판)", example = "FREE")
        @NotNull(message = "게시판 타입은 필수입니다.")
        BoardType boardType,

        @Schema(description = "과목 ID (QUESTION 게시판일 경우 필수)", example = "10")
        Long subjectId,

        @Schema(description = "게시글 제목 (300자 이하)", example = "Spring Security 질문드립니다.")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 300, message = "제목은 300자 이하여야 합니다.")
        String title,

        @Schema(description = "게시글 내용", example = "JWT 필터 순서가 헷갈리는데 설명 부탁드립니다.")
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {}