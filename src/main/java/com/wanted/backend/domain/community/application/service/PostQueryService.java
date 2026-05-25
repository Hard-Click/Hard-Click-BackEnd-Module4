package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.usecase.PostQueryUseCase;
import com.wanted.backend.domain.community.domain.model.*;
import com.wanted.backend.domain.community.domain.repository.PostFileRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ViewLogRepository;
import com.wanted.backend.domain.community.presentation.response.PostDetailResponse;
import com.wanted.backend.domain.community.presentation.response.PostItemResponse;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;



@Service
@Transactional
public class PostQueryService implements PostQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final ViewLogRepository viewLogRepository;
    private final MemberNamePort memberNamePort;

    public PostQueryService(PostRepository postRepository,
                            PostFileRepository postFileRepository,
                            ViewLogRepository viewLogRepository,
                            MemberNamePort memberNamePort) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.viewLogRepository = viewLogRepository;
        this.memberNamePort = memberNamePort;
    }

    @Override
    public PostListResponse getList(BoardType boardType, PostSortType sort,
                                    String keyword, int page) {
        List<Post> posts = boardType != null
                ? postRepository.findByBoardType(boardType, sort, keyword, page, PAGE_SIZE)
                : postRepository.findAll(sort, keyword, page, PAGE_SIZE);

        int totalCount = boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);

        List<PostItemResponse> items = posts.stream()
                .map(this::toItemResponse)
                .toList();

        return new PostListResponse(
                items,
                page,
                (int) Math.ceil((double) totalCount / PAGE_SIZE),
                totalCount
        );
    }

    @Override
    @Transactional
    public PostDetailResponse getDetail(Long postId, Long memberId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        boolean alreadyViewed = viewLogRepository
                .existsByMemberIdAndPostIdAndViewedAtAfter(memberId, postId, thirtyMinutesAgo);

        if (!alreadyViewed) {
            post.increaseViewCount();

            postRepository.updateViewCount(postId, post.getViewCount());

            viewLogRepository.save(ViewLog.create(memberId, postId));
        }

        String name = memberNamePort.getNameByMemberId(post.getAuthorId());

        List<String> fileUrls = postFileRepository.findByPostId(postId)
                .stream()
                .map(PostFile::getFileUrl)
                .toList();

        return new PostDetailResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                Review.maskName(name),
                post.getCreatedAt(),
                post.getViewCount(),
                post.getContent(),
                post.getAuthorId().equals(memberId),
                post.isAccepted(),
                fileUrls
        );
    }

    private PostItemResponse toItemResponse(Post post) {
        String name = memberNamePort.getNameByMemberId(post.getAuthorId());
        return new PostItemResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                Review.maskName(name),
                post.getCreatedAt(),
                post.getViewCount(),
                0
        );
    }
}