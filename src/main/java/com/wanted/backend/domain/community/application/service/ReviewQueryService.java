package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.result.RatingStatResult;
import com.wanted.backend.domain.community.application.result.ReviewItemResult;
import com.wanted.backend.domain.community.application.result.ReviewListResult;
import com.wanted.backend.domain.community.application.usecase.ReviewQueryUseCase;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService implements ReviewQueryUseCase {

    private static final int PAGE_SIZE = 10;
    private static final String ADMIN_DELETED_MESSAGE = "관리자에 의해 삭제되었습니다.";

    private final ReviewRepository reviewRepository;
    private final MemberNamePort memberNamePort;
    private final CommunityAccessPolicy communityAccessPolicy;

    public ReviewQueryService(ReviewRepository reviewRepository,
                              MemberNamePort memberNamePort,
                              CommunityAccessPolicy communityAccessPolicy) {
        this.reviewRepository = reviewRepository;
        this.memberNamePort = memberNamePort;
        this.communityAccessPolicy = communityAccessPolicy;
    }

    @Override
    public ReviewListResult handle(Long courseId, Long memberId, ReviewSortType sort, int page) {
        List<Review> myReview = new ArrayList<>();
        // 비회원(-1)은 통과, 로그인한 정지/탈퇴 회원만 차단
        communityAccessPolicy.validateAccessIfLoggedIn(memberId);

        if (!memberId.equals(-1L)) {
            reviewRepository.findByCourseIdAndMemberId(courseId, memberId)
                    .ifPresent(myReview::add);
        }

        List<Review> others = !memberId.equals(-1L)
                ? reviewRepository.findByCourseIdExcludeMember(courseId, memberId, sort, page, PAGE_SIZE)
                : reviewRepository.findByCourseId(courseId, sort, page, PAGE_SIZE);

        List<Review> allReviews = new ArrayList<>(myReview);
        allReviews.addAll(others);

        List<Long> memberIds = allReviews.stream()
                .map(Review::getMemberId)
                .collect(Collectors.toList());
        Map<Long, String> nameMap = memberNamePort.getNamesByMemberIds(memberIds);

        List<ReviewItemResult> items = new ArrayList<>();
        myReview.forEach(r -> items.add(toItemResult(r, memberId, nameMap)));
        others.forEach(r -> items.add(toItemResult(r, memberId, nameMap)));

        int totalCount = reviewRepository.countByCourseId(courseId);

        List<RatingStatResult> ratingStats = reviewRepository.countGroupByRating(courseId).stream()
                .map(rc -> new RatingStatResult(rc.rating(), rc.count()))
                .toList();

        return new ReviewListResult(
                reviewRepository.avgRatingByCourseId(courseId),
                totalCount, ratingStats, items, page,
                (int) Math.ceil((double) totalCount / PAGE_SIZE));
    }

    private ReviewItemResult toItemResult(Review review, Long currentMemberId, Map<Long, String> nameMap) {
        String name = nameMap.getOrDefault(review.getMemberId(), "");
        return new ReviewItemResult(
                review.getId(),
                Review.maskName(name),
                name.isEmpty() ? "" : name.substring(0, 1),
                review.getRating(),
                review.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : review.getContent(),
                review.getCreatedAt().toLocalDate(),
                review.isOwner(currentMemberId));
    }
}