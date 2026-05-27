package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.presentation.response.CommentListResponse;


public interface CommentQueryUseCase {
    CommentListResponse getComments(Long postId, Long memberId);
}