package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    // boardType + 키워드 검색 + 페이징
    List<PostJpaEntity> findByBoardTypeAndTitleContainingAndStatus(
            BoardType boardType, String keyword, PostStatus status, Pageable pageable);

    // 전체 + 키워드 검색 + 페이징
    List<PostJpaEntity> findByTitleContainingAndStatus(
            String keyword, PostStatus status, Pageable pageable);

    // boardType + 키워드 검색 전체 수
    int countByBoardTypeAndTitleContainingAndStatus(
            BoardType boardType, String keyword, PostStatus status);

    // 전체 + 키워드 검색 전체 수
    int countByTitleContainingAndStatus(String keyword, PostStatus status);

    List<PostJpaEntity> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, PostStatus status);


}
