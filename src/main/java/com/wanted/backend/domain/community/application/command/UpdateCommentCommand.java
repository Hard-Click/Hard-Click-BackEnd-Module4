package com.wanted.backend.domain.community.application.command;

import org.springframework.web.multipart.MultipartFile;


public record UpdateCommentCommand(
        Long memberId,
        Long commentId,
        String content,
        MultipartFile file
) {

}