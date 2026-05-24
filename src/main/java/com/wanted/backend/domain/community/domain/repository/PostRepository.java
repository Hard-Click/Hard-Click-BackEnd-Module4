package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostSortType;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    // 게시판 타입별 목록 페이징 조회
    List<Post> findByBoardType(BoardType boardType, PostSortType sort,
                               String keyword, int page, int size);

    // 전체 목록 페이징 조회
    List<Post> findAll(PostSortType sort, String keyword, int page, int size);

    // 전체 게시글 수 (페이징 계산용)
    int countByBoardType(BoardType boardType, String keyword);

    // 전체 게시글 수
    int countAll(String keyword);
}