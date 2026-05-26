package com.wanted.backend.domain.community.application.command;

import org.springframework.web.multipart.MultipartFile;

public record CreateCommentCommand(
        Long memberId,
        Long postId,
        Long parentId,
        String content,
        MultipartFile file
) {

}