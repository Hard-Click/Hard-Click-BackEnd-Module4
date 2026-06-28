package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.presentation.response.PostDetailResponse;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;

public interface PostQueryUseCase {

    // 게시글 목록 조회
    PostListResponse getList(BoardType boardType, PostSortType sort, String keyword, int page, boolean isAdmin, Long memberId);
    PostDetailResponse getDetail(Long postId, Long memberId, boolean isAdmin);
}