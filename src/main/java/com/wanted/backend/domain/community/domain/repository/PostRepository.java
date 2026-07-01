package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.domain.model.PostSummary;

import java.util.List;
import java.util.Optional;


public interface PostRepository {

    // 게시글 저장
    Post save(Post post);

    // 단건 조회 (상세 조회, 수정, 삭제용)
    Optional<Post> findById(Long postId);

    // 게시판 타입별 목록 페이징 조회
    List<Post> findByBoardType(BoardType boardType, PostSortType sort,
                               String keyword, int page, int size);

    // 전체 목록 페이징 조회
    List<Post> findAll(PostSortType sort, String keyword, int page, int size);

    // 게시판 타입별 전체 수 (페이징 계산용)
    int countByBoardType(BoardType boardType, String keyword);

    // 전체 수
    int countAll(String keyword);

    // 조회수 업데이트
    void updateViewCount(Long postId, int viewCount);

    //게시글 삭제
    void deleteById(Long postId);

    // 방법③: JOIN + DTO Projection — 게시글+작성자명+댓글수 1쿼리
    List<PostSummary> findSummariesByBoardType(BoardType boardType, String keyword, int page, int size);
    List<PostSummary> findAllSummaries(String keyword, int page, int size);
}