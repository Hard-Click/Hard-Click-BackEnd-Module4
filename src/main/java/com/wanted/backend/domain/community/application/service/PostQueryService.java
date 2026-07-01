package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.usecase.PostQueryUseCase;
import com.wanted.backend.domain.community.domain.model.*;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostFileRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ViewLogRepository;
import com.wanted.backend.domain.community.presentation.response.PostDetailResponse;
import com.wanted.backend.domain.community.presentation.response.PostItemResponse;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class PostQueryService implements PostQueryUseCase {

    private static final int PAGE_SIZE = 10;

    // benchmark.method=before|method2|method3 (기본값 method3)
    @Value("${benchmark.method:method3}")
    private String benchmarkMethod;

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final ViewLogRepository viewLogRepository;
    private final MemberNamePort memberNamePort;
    private final CommentRepository commentRepository;

    public PostQueryService(PostRepository postRepository,
                            PostFileRepository postFileRepository,
                            ViewLogRepository viewLogRepository,
                            MemberNamePort memberNamePort, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.viewLogRepository = viewLogRepository;
        this.memberNamePort = memberNamePort;
        this.commentRepository = commentRepository;
    }

    @Override
    public PostListResponse getList(BoardType boardType, PostSortType sort, String keyword, int page) {
        int totalCount = boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        return switch (benchmarkMethod) {
            case "method3" -> getListMethod3(boardType, sort, keyword, page, totalCount, totalPages);
            case "method2" -> getListMethod2(boardType, sort, keyword, page, totalCount, totalPages);
            default        -> getListBefore(boardType, sort, keyword, page, totalCount, totalPages);
        };
    }

    // ── 방법③: JOIN + DTO Projection (1쿼리, 상관 서브쿼리 제거) ──────────────────────
    private PostListResponse getListMethod3(BoardType boardType, PostSortType sort,
                                            String keyword, int page, int total, int totalPages) {
        if (sort == PostSortType.comments) {
            List<PostSummary> summaries = boardType != null
                    ? postRepository.findSummariesByBoardType(boardType, keyword, page, PAGE_SIZE)
                    : postRepository.findAllSummaries(keyword, page, PAGE_SIZE);

            List<PostItemResponse> items = summaries.stream()
                    .map(s -> new PostItemResponse(
                            s.getId(), s.getBoardType(), s.getTitle(),
                            Review.maskName(s.getAuthorName()),
                            s.getCreatedAt(), s.getViewCount(), (int) s.getCommentCount()))
                    .collect(Collectors.toList());

            return new PostListResponse(items, page, totalPages, total);
        }
        // comments 외 정렬: 방법②와 동일 (배치 IN)
        return getListMethod2(boardType, sort, keyword, page, total, totalPages);
    }

    // ── 방법②: Batch IN + Map (3쿼리, N+1 제거) ────────────────────────────────────
    private PostListResponse getListMethod2(BoardType boardType, PostSortType sort,
                                            String keyword, int page, int total, int totalPages) {
        List<Post> posts = boardType != null
                ? postRepository.findByBoardType(boardType, sort, keyword, page, PAGE_SIZE)
                : postRepository.findAll(sort, keyword, page, PAGE_SIZE);

        Set<Long> authorIds = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());
        Map<Long, String> nameMap = memberNamePort.getNamesByMemberIds(authorIds);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> cntMap = commentRepository.countsByPostIds(postIds);

        List<PostItemResponse> items = posts.stream()
                .map(p -> new PostItemResponse(
                        p.getId(), p.getBoardType(), p.getTitle(),
                        Review.maskName(nameMap.getOrDefault(p.getAuthorId(), "")),
                        p.getCreatedAt(), p.getViewCount(),
                        cntMap.getOrDefault(p.getId(), 0L).intValue()))
                .collect(Collectors.toList());

        return new PostListResponse(items, page, totalPages, total);
    }

    // ── Before: N+1 원본 (21쿼리) ────────────────────────────────────────────────
    private PostListResponse getListBefore(BoardType boardType, PostSortType sort,
                                           String keyword, int page, int total, int totalPages) {
        List<Post> posts = boardType != null
                ? postRepository.findByBoardType(boardType, sort, keyword, page, PAGE_SIZE)
                : postRepository.findAll(sort, keyword, page, PAGE_SIZE);

        List<PostItemResponse> items = posts.stream()
                .map(this::toItemResponse)
                .toList();

        return new PostListResponse(items, page, totalPages, total);
    }

    @Override
    @Transactional
    public PostDetailResponse getDetail(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (memberId != null) {
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
            boolean alreadyViewed = viewLogRepository
                    .existsByMemberIdAndPostIdAndViewedAtAfter(memberId, postId, thirtyMinutesAgo);

            if (!alreadyViewed) {
                post.increaseViewCount();
                postRepository.updateViewCount(postId, post.getViewCount());
                viewLogRepository.save(ViewLog.create(memberId, postId));
            }
        }

        String name = memberNamePort.getNameByMemberId(post.getAuthorId());

        List<String> fileUrls = postFileRepository.findByPostId(postId)
                .stream()
                .map(PostFile::getFileUrl)
                .toList();

        return new PostDetailResponse(
                post.getId(), post.getBoardType(), post.getTitle(),
                Review.maskName(name), post.getCreatedAt(), post.getViewCount(),
                post.getContent(), post.isOwner(memberId), post.isAccepted(), fileUrls);
    }

    // Before N+1: 게시글 1건마다 2쿼리 (이름 1 + 댓글수 1)
    private PostItemResponse toItemResponse(Post post) {
        String name = memberNamePort.getNameByMemberId(post.getAuthorId());
        int commentCount = commentRepository.countByPostId(post.getId());
        return new PostItemResponse(
                post.getId(), post.getBoardType(), post.getTitle(),
                Review.maskName(name), post.getCreatedAt(), post.getViewCount(), commentCount);
    }
}
