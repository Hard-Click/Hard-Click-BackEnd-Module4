package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.usecase.ReviewQueryUseCase;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.domain.community.presentation.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService implements ReviewQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final ReviewRepository reviewRepository;
    private final MemberNamePort memberNamePort;

    public ReviewQueryService(ReviewRepository reviewRepository,
                              MemberNamePort memberNamePort) {
        this.reviewRepository = reviewRepository;
        this.memberNamePort = memberNamePort;
    }

    @Override
    public ReviewListResponse handle(Long courseId, Long memberId, ReviewSortType sort, int page) {

        // 1. 본인 리뷰 최상단 고정
        List<ReviewItemResponse> items = new ArrayList<>();

        if (!memberId.equals(-1L)) {
            reviewRepository.findByCourseIdAndMemberId(courseId, memberId)
                    .ifPresent(r -> items.add(toItemResponse(r, memberId)));
        }

        // 2. 나머지 리뷰 페이징
        List<Review> others = !memberId.equals(-1L)
                ? reviewRepository.findByCourseIdExcludeMember(courseId, memberId, sort, page, PAGE_SIZE)
                : reviewRepository.findByCourseId(courseId, sort, page, PAGE_SIZE);

        others.forEach(r -> items.add(toItemResponse(r, memberId)));

        // 3. 통계 조립
        int totalCount = reviewRepository.countByCourseId(courseId);

        return new ReviewListResponse(
                reviewRepository.avgRatingByCourseId(courseId),
                totalCount,
                reviewRepository.countGroupByRating(courseId).stream()
                        .map(rc -> new RatingStatItem(rc.rating(), rc.count()))
                        .toList(),
                items,
                page,
                (int) Math.ceil((double) totalCount / PAGE_SIZE)
        );
    }

    private ReviewItemResponse toItemResponse(Review review, Long currentMemberId) {
        String name = memberNamePort.getNameByMemberId(review.getMemberId());
        return new ReviewItemResponse(
                review.getId(),
                review.maskName(name),
                name.substring(0, 1),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt().toLocalDate(),
                review.isOwner(currentMemberId)
        );
    }
}