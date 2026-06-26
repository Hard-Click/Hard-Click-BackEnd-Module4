package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.usecase.CommentQueryUseCase;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.presentation.response.CommentListResponse;
import com.wanted.backend.domain.community.presentation.response.CommentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class CommentQueryService implements CommentQueryUseCase {

    private final CommentRepository commentRepository;
    private final MemberNamePort memberNamePort;
    private final CommunityFileStoragePort fileStoragePort;

    public CommentQueryService(CommentRepository commentRepository,
                               MemberNamePort memberNamePort,
                               CommunityFileStoragePort fileStoragePort) {
        this.commentRepository = commentRepository;
        this.memberNamePort = memberNamePort;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public CommentListResponse getComments(Long postId, Long memberId, boolean isAdmin) {

        // 1. 원댓글 목록 조회
        List<Comment> parentComments = commentRepository
                .findByPostIdAndParentIdIsNull(postId);

        // 2. 채택된 댓글 최상단 + 나머지 최신순 정렬
        List<CommentResponse> comments = parentComments.stream()
                .sorted(Comparator
                        .comparing(Comment::isAccepted).reversed()
                        .thenComparing(Comparator.comparing(Comment::getCreatedAt).reversed()))
                .map(comment -> toResponse(comment, memberId, isAdmin))
                .toList();

        return new CommentListResponse(comments.size(), comments);
    }

    private CommentResponse toResponse(Comment comment, Long currentMemberId, boolean isAdmin) {

        // 삭제된 댓글은 작성자 정보 마스킹
        String rawName = comment.isDeleted()
                ? null
                : memberNamePort.getNameByMemberId(comment.getAuthorId()); // 딱 1번만

        String authorName = rawName == null ? "" : (isAdmin ? rawName : Review.maskName(rawName));
        String authorInitial = rawName == null ? "" : rawName.substring(0, 1);

        // 대댓글 목록 조회 (최신순)
        List<CommentResponse> replies = commentRepository.findByParentId(comment.getId())
                .stream()
                .map(reply -> toResponse(reply, currentMemberId, isAdmin))
                .toList();

        return new CommentResponse(
                comment.getId(),
                authorName,
                authorInitial,
                comment.isAdminDeleted() ? Comment.ADMIN_DELETED_MESSAGE : comment.getContent(),
                comment.getCreatedAt(),
                comment.isAccepted(),
                comment.getAuthorId().equals(currentMemberId),  // 본인 댓글 여부
                comment.isDeleted(),
                fileStoragePort.presignUrl(comment.getImageUrl()),
                replies
        );
    }
}
