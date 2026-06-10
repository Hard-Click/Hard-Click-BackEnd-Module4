package com.wanted.backend.domain.community.presentation.response;


import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCommentResponse(

        @Schema(description = "작성된 댓글 ID", example = "12")
        Long commentId

) {

}