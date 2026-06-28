package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.result.PostDetailResult;
import com.wanted.backend.domain.community.application.result.PostListResult;
import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostSortType;

public interface PostQueryUseCase {
    PostListResult getList(BoardType boardType, PostSortType sort, String keyword, int page, boolean isAdmin);
    PostDetailResult getDetail(Long postId, Long memberId, boolean isAdmin);
}