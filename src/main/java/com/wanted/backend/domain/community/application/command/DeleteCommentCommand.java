package com.wanted.backend.domain.community.application.command;

public record DeleteCommentCommand(
        Long memberId,
        Long commentId,
        boolean isAdmin
) {

}