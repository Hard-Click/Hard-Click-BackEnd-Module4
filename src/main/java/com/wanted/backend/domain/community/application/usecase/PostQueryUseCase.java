package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;

public interface PostQueryUseCase {
    PostListResponse getList(BoardType boardType, PostSortType sort,
                             String keyword, int page);
}