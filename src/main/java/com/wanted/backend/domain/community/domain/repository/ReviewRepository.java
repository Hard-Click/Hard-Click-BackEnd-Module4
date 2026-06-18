package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    // 리뷰 저장
    Review save(Review review);

    // 중복 리뷰 검증용
    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);

    // 본인 리뷰 단건 조회 (최상단 고정용)
    Optional<Review> findByCourseIdAndMemberId(Long courseId, Long memberId);

    // 본인 제외 나머지 리뷰 페이징 조회
    List<Review> findByCourseIdExcludeMember(Long courseId, Long memberId,
                                             ReviewSortType sort, int page, int size);

    // 비로그인용 전체 리뷰 페이징 조회
    List<Review> findByCourseId(Long courseId, ReviewSortType sort, int page, int size);

    // 별점 분포 집계
    List<RatingCount> countGroupByRating(Long courseId);

    // 총 리뷰 수
    int countByCourseId(Long courseId);

    // 평균 별점
    Double avgRatingByCourseId(Long courseId);

    // 수정/삭제용 단건 조회 추가
    Optional<Review> findById(Long reviewId);

    //삭제용
    void deleteById(Long reviewId);

    void adminDeleteById(Long reviewId);

    record RatingCount(Integer rating, Long count) {}
}
