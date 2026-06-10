package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.MyActivityQueryPort;
import com.wanted.backend.domain.community.application.usecase.GetMyActivityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyActivityService implements GetMyActivityUseCase {

    private final MyActivityQueryPort myActivityQueryPort;

    @Override
    public MyActivityView handle(Long memberId) {
        MyActivityQueryPort.MyActivityData data = myActivityQueryPort.findByMemberId(memberId);

        return new MyActivityView(
                data.posts().stream()
                        .map(post -> new MyPostActivity(
                                post.postId(),
                                post.boardType(),
                                post.title(),
                                post.viewCount(),
                                post.accepted(),
                                post.createdAt()
                        ))
                        .toList(),
                data.comments().stream()
                        .map(comment -> new MyCommentActivity(
                                comment.commentId(),
                                comment.postId(),
                                comment.parentId(),
                                comment.content(),
                                comment.accepted(),
                                comment.createdAt()
                        ))
                        .toList(),
                data.reviews().stream()
                        .map(review -> new MyReviewActivity(
                                review.reviewId(),
                                review.courseId(),
                                review.rating(),
                                review.content(),
                                review.createdAt()
                        ))
                        .toList()
        );
    }
}
