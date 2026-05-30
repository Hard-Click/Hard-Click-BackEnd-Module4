package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePostResponse(

        @Schema(description = "수정된 게시글 ID", example = "37")
        Long postId

) {

}