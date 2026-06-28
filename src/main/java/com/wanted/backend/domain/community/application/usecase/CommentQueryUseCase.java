package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.result.CommentListResult;

public interface CommentQueryUseCase {
    CommentListResult getComments(Long postId, Long memberId, boolean isAdmin);
}