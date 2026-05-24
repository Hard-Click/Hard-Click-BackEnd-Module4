package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.usecase.PostQueryUseCase;
import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.presentation.response.PostItemResponse;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostQueryService implements PostQueryUseCase {

    private static final int PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final MemberNamePort memberNamePort;

    public PostQueryService(PostRepository postRepository,
                            MemberNamePort memberNamePort) {
        this.postRepository = postRepository;
        this.memberNamePort = memberNamePort;
    }

    @Override
    public PostListResponse getList(BoardType boardType, PostSortType sort,
                                    String keyword, int page) {

        // boardType null이면 전체 조회, 있으면 해당 게시판 조회
        List<Post> posts = boardType != null
                ? postRepository.findByBoardType(boardType, sort, keyword, page, PAGE_SIZE)
                : postRepository.findAll(sort, keyword, page, PAGE_SIZE);

        int totalCount = boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);

        // 게시글 목록 → Response 변환
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

    private PostItemResponse toItemResponse(Post post) {
        // identity BC에서 작성자명 조회 후 마스킹
        String name = memberNamePort.getNameByMemberId(post.getAuthorId());

        return new PostItemResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                Review.maskName(name),  // 마스킹 처리
                post.getCreatedAt(),
                post.getViewCount(),
                0   // 댓글수 - 추후 구현
        );
    }
}