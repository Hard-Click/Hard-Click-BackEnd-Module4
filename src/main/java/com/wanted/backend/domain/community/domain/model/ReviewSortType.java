package com.wanted.backend.domain.community.domain.model;

/*
 * [Domain Layer - Enum]
 * 리뷰 목록 정렬 방식을 나타내는 열거형이다.
 *
 * latest: 최신순 (created_at DESC)
 * rating: 별점순 (rating DESC)
 */
public enum ReviewSortType {
    latest,
    rating
}