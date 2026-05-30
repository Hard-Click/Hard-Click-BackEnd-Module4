package com.wanted.backend.domain.community.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreatePostResponse(

        @Schema(description = "작성된 게시글 ID", example = "37")
        Long postId

) {}