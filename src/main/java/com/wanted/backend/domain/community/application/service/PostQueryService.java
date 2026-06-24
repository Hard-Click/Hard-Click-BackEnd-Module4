package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;



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
    private final CommunityAccessPolicy communityAccessPolicy;
    private final CommunityFileStoragePort fileStoragePort;

    public PostQueryService(PostRepository postRepository,
                            PostFileRepository postFileRepository,
                            ViewLogRepository viewLogRepository,
                            MemberNamePort memberNamePort, CommentRepository commentRepository, CommunityAccessPolicy communityAccessPolicy, CommunityFileStoragePort fileStoragePort) {
                          
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.viewLogRepository = viewLogRepository;
        this.memberNamePort = memberNamePort;
        this.commentRepository = commentRepository;
        this.communityAccessPolicy = communityAccessPolicy;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public PostListResponse getList(BoardType boardType, PostSortType sort,
                                    String keyword, int page, Long memberId) {
        if (memberId != null) communityAccessPolicy.validateAccess(memberId);
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

        if (memberId != null) communityAccessPolicy.validateAccess(memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() == PostStatus.DELETED) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // memberId가 null이면 조회수 트래킹 스킵
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

        List<String> fileUrls = post.isAdminDeleted()
                ? List.of()
                : postFileRepository.findByPostId(postId)
                .stream()
                .map(PostFile::getFileUrl)
                .map(fileStoragePort::presignUrl)
                .toList();

        return new PostDetailResponse(
                post.getId(),
                post.getBoardType(),
                post.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : post.getTitle(),
                Review.maskName(name),
                post.getCreatedAt(),
                post.getViewCount(),
                post.isAdminDeleted() ? ADMIN_DELETED_MESSAGE : post.getContent(),
                post.isOwner(memberId),   // Post 도메인 모델에 위임
                post.isAccepted(),
                fileUrls,
                post.getSubject()
        );
    }

    private PostItemResponse toItemResponse(Post post) {
        String name = memberNamePort.getNameByMemberId(post.getAuthorId());
        int commentCount = commentRepository.countByPostId(post.getId());
        return new PostItemResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                Review.maskName(name),
                post.getCreatedAt(),
                post.getViewCount(),
                commentCount
        );
    }
}
