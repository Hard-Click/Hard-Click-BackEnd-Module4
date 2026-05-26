package com.wanted.backend.domain.community.application.command;

public record AcceptCommentCommand(
        Long memberId,
        Long commentId
) {

}