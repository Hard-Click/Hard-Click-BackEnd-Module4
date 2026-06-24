package com.wanted.backend.domain.community.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;


public record CommentResponse(

        @Schema(description = "댓글 ID", example = "12")
        Long commentId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성자 이름 첫 글자 (아바타용)", example = "홍")
        String authorInitial,

        @Schema(description = "댓글 내용", example = "JWT 필터는 OncePerRequestFilter를 상속받아 구현합니다.")
        String content,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss+09:00")
        @Schema(description = "작성일시", example = "2025-03-15T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "채택된 댓글 여부", example = "false")
        boolean isAccepted,

        @Schema(description = "본인이 작성한 댓글 여부", example = "false")
        boolean isMine,

        @Schema(description = "삭제된 댓글 여부", example = "false")
        boolean isDeleted,

        @Schema(description = "첨부 이미지 URL", example = "http://localhost:8080/files/comment/abc123.jpg")
        String imageUrl,

        @Schema(description = "대댓글 목록")
        List<CommentResponse> replies
) {

}