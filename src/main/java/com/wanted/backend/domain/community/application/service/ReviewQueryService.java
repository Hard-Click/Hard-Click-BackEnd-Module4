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
    private static final String ADMIN_DELETED_MESSAGE = "관리자에 의해 삭제되었습니다.";

    private final ReviewRepository reviewRepository;
    private final MemberNamePort memberNamePort;

    public ReviewQueryService(ReviewRepository reviewRepository,
                              MemberNamePort memberNamePort) {
        this.reviewRepository = reviewRepository;
        this.memberNamePort = memberNamePort;
    }

    @Override
    public ReviewListResponse handle(Long courseId, Long memberId, ReviewSortType sort, int page) {


        List<ReviewItemResponse> items = new ArrayList<>();

        //memberId 하드코딩 아니면 자기가 쓴거 List 최상위 노출
        if (!memberId.equals(-1L)) {
            reviewRepository.findByCourseIdAndMemberId(courseId, memberId)
                    .ifPresent(r -> items.add(toItemResponse(r, memberId)));
        }

        //나머지 리뷰 페이징
        List<Review> others = !memberId.equals(-1L)
                ? reviewRepository.findByCourseIdExcludeMember(courseId, memberId, sort, page, PAGE_SIZE)
                : reviewRepository.findByCourseId(courseId, sort, page, PAGE_SIZE);

        //이미 있는 리스트에 add 하는 값들
        others.forEach(r -> items.add(toItemResponse(r, memberId)));

        // 3. 통계 조립
        int totalCount = reviewRepository.countByCourseId(courseId);

        return new ReviewListResponse(
                reviewRepository.avgRatingByCourseId(courseId),
                totalCount,
                //list로 형변환 해서 받아온 값을 응답 객체인 RatingStatItem로 변환하는 작업
                reviewRepository.countGroupByRating(courseId).stream()
                        .map(rc -> new RatingStatItem(rc.rating(), rc.count()))
                        .toList(),
                items,
                page,
                (int) Math.ceil((double) totalCount / PAGE_SIZE)
        );
    }

    private ReviewItemResponse toItemResponse(Review review, Long currentMemberId) {
        //바로 port를 호출하는 이유는 단순 다른 BC에서의 조회이기때문에 정책 클래스인 Policy 클래스 존재 필요 없음
        String name = memberNamePort.getNameByMemberId(review.getMemberId());
        return new ReviewItemResponse(
                review.getId(),
                review.maskName(name),
                name.substring(0, 1),
                review.getRating(),
                review.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : review.getContent(),
                review.getCreatedAt().toLocalDate(),
                review.isOwner(currentMemberId)
        );
    }
}
