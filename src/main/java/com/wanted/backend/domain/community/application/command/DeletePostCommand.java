package com.wanted.backend.domain.community.application.command;

public record DeletePostCommand(
        Long memberId,
        Long postId,
        boolean isAdmin
) {

}