package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.result.PostDetailResult;
import com.wanted.backend.domain.community.application.result.PostItemResult;
import com.wanted.backend.domain.community.application.result.PostListResult;
import com.wanted.backend.domain.community.application.usecase.PostQueryUseCase;
import com.wanted.backend.domain.community.domain.model.*;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostFileRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ViewLogRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostQueryService implements PostQueryUseCase {

    private static final int PAGE_SIZE = 10;
    private static final String ADMIN_DELETED_MESSAGE = "관리자에 의해 삭제되었습니다.";

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final ViewLogRepository viewLogRepository;
    private final MemberNamePort memberNamePort;
    private final CommentRepository commentRepository;
    private final CommunityFileStoragePort fileStoragePort;
    private final MeterRegistry meterRegistry;
    private final CommunityAccessPolicy communityAccessPolicy;

    public PostQueryService(PostRepository postRepository,
                            PostFileRepository postFileRepository,
                            ViewLogRepository viewLogRepository,
                            MemberNamePort memberNamePort,
                            CommentRepository commentRepository,
                            CommunityFileStoragePort fileStoragePort,
                            MeterRegistry meterRegistry,
                            CommunityAccessPolicy communityAccessPolicy) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.viewLogRepository = viewLogRepository;
        this.memberNamePort = memberNamePort;
        this.commentRepository = commentRepository;
        this.fileStoragePort = fileStoragePort;
        this.meterRegistry = meterRegistry;
        this.communityAccessPolicy = communityAccessPolicy;
    }

    @Override
    public PostListResult getList(BoardType boardType, PostSortType sort,
                                  String keyword, int page, boolean isAdmin, Long memberId) {
        communityAccessPolicy.validateAccessIfLoggedIn(memberId);

        List<Post> posts = boardType != null
                ? postRepository.findByBoardType(boardType, sort, keyword, page, PAGE_SIZE)
                : postRepository.findAll(sort, keyword, page, PAGE_SIZE);

        int totalCount = boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);

        // 작성자명 + 댓글 수 일괄 조회 — N+1 제거
        Set<Long> authorIds = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());
        Map<Long, String> nameMap = memberNamePort.getNamesByMemberIds(authorIds);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> commentCountMap = commentRepository.countsByPostIds(postIds);

        List<PostItemResult> items = posts.stream()
                .map(post -> toItemResult(post, isAdmin, nameMap, commentCountMap))
                .toList();

        return new PostListResult(
                items, page,
                (int) Math.ceil((double) totalCount / PAGE_SIZE),
                totalCount);
    }

    @Override
    @Transactional
    public PostDetailResult getDetail(Long postId, Long memberId, boolean isAdmin) {
        communityAccessPolicy.validateAccessIfLoggedIn(memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        if (memberId != null) {
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

            Timer.Sample duplicateCheckSample = Timer.start(meterRegistry);
            boolean alreadyViewed = viewLogRepository
                    .existsByMemberIdAndPostIdAndViewedAtAfter(memberId, postId, thirtyMinutesAgo);
            duplicateCheckSample.stop(Timer.builder("post.view.duplicate.check")
                    .publishPercentileHistogram(true)
                    .register(meterRegistry));

            if (!alreadyViewed) {
                Timer.Sample updateSample = Timer.start(meterRegistry);
                post.increaseViewCount();
                postRepository.updateViewCount(postId, post.getViewCount());
                viewLogRepository.save(ViewLog.create(memberId, postId));
                updateSample.stop(Timer.builder("post.view.count.update")
                        .publishPercentileHistogram(true)
                        .register(meterRegistry));
            }
        }

        String name = memberNamePort.getNameByMemberId(post.getAuthorId());
        String displayName = isAdmin ? name : Review.maskName(name);

        List<String> fileUrls = post.isAdminDeleted()
                ? List.of()
                : postFileRepository.findByPostId(postId).stream()
                .map(PostFile::getFileUrl)
                .map(fileStoragePort::presignUrl)
                .toList();

        return new PostDetailResult(
                post.getId(),
                post.getBoardType(),
                post.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : post.getTitle(),
                displayName,
                post.getCreatedAt(),
                post.getViewCount(),
                post.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : post.getContent(),
                post.isOwner(memberId),
                post.isAccepted(),
                fileUrls,
                post.getSubject());
    }

    private PostItemResult toItemResult(Post post, boolean isAdmin,
                                        Map<Long, String> nameMap, Map<Long, Long> commentCountMap) {
        String name = nameMap.getOrDefault(post.getAuthorId(), "");
        String displayName = isAdmin ? name : Review.maskName(name);
        int commentCount = commentCountMap.getOrDefault(post.getId(), 0L).intValue();
        return new PostItemResult(
                post.getId(), post.getBoardType(),
                post.getBoardType() == BoardType.QUESTION ? post.getSubject() : null,
                post.getTitle(), displayName, post.getCreatedAt(),
                post.getViewCount(), commentCount);
    }
}