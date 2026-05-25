package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.PostFile;

import java.util.List;

public interface PostFileRepository {

    // 첨부파일 저장
    PostFile save(PostFile postFile);

    // 게시글 첨부파일 목록 조회 (sortOrder 오름차순)
    List<PostFile> findByPostId(Long postId);
}