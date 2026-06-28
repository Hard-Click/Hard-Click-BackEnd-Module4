package com.wanted.backend.domain.community.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wanted.backend.domain.community.application.result.PostDetailResult;
import com.wanted.backend.domain.community.domain.model.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(

        @Schema(description = "게시글 ID", example = "37")
        Long postId,

        @Schema(description = "게시판 타입 (FREE: 자유게시판, QUESTION: 질문게시판)", example = "QUESTION")
        BoardType boardType,

        @Schema(description = "게시글 제목", example = "Spring Security 질문드립니다.")
        String title,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        @Schema(description = "작성일시", example = "2025-03-15T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "조회수", example = "142")
        int viewCount,

        @Schema(description = "게시글 내용", example = "JWT 필터 순서가 헷갈리는데 설명 부탁드립니다.")
        String content,

        @Schema(description = "본인이 작성한 게시글 여부", example = "false")
        boolean isMyPost,

        @Schema(description = "채택 여부", example = "false")
        boolean isAccepted,

        @Schema(description = "첨부 파일 URL 목록(2개까지만)", example = "[\"http://localhost:8080/files/post/abc123.jpg\"]")
        List<String> fileUrls,

        @Schema(description = "과목 — QUESTION 게시판일 경우 SubjectType enum 값 반환, FREE 게시판은 null", example = "MATH_1")
        String subject
) {
        public static PostDetailResponse from(PostDetailResult result) {
                return new PostDetailResponse(
                        result.postId(), result.boardType(), result.title(),
                        result.authorName(), result.createdAt(), result.viewCount(),
                        result.content(), result.isMyPost(), result.isAccepted(),
                        result.fileUrls(), result.subject());
        }
}