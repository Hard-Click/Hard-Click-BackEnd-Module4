package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.result.CommentListResult;
import com.wanted.backend.domain.community.application.result.CommentResult;
import com.wanted.backend.domain.community.application.usecase.CommentQueryUseCase;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
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
    private final CommunityAccessPolicy communityAccessPolicy;

    public CommentQueryService(CommentRepository commentRepository,
                               MemberNamePort memberNamePort,
                               CommunityFileStoragePort fileStoragePort,
                               CommunityAccessPolicy communityAccessPolicy) {
        this.commentRepository = commentRepository;
        this.memberNamePort = memberNamePort;
        this.fileStoragePort = fileStoragePort;
        this.communityAccessPolicy = communityAccessPolicy;
    }

    @Override
    public CommentListResult getComments(Long postId, Long memberId, boolean isAdmin) {
        communityAccessPolicy.validateAccessIfLoggedIn(memberId);

        List<Comment> parentComments = commentRepository.findByPostIdAndParentIdIsNull(postId);

        List<CommentResult> comments = parentComments.stream()
                .sorted(Comparator
                        .comparing(Comment::isAccepted).reversed()
                        .thenComparing(Comparator.comparing(Comment::getCreatedAt).reversed()))
                .map(comment -> toResult(comment, memberId, isAdmin))
                .toList();

        return new CommentListResult(commentRepository.countByPostId(postId), comments);
    }

    private CommentResult toResult(Comment comment, Long currentMemberId, boolean isAdmin) {
        String rawName = comment.isDeleted()
                ? null
                : memberNamePort.getNameByMemberId(comment.getAuthorId());

        String authorName = rawName == null ? "" : (isAdmin ? rawName : Review.maskName(rawName));
        String authorInitial = rawName == null ? "" : rawName.substring(0, 1);

        List<CommentResult> replies = commentRepository.findByParentId(comment.getId())
                .stream()
                .map(reply -> toResult(reply, currentMemberId, isAdmin))
                .toList();

        return new CommentResult(
                comment.getId(),
                authorName,
                authorInitial,
                comment.isAdminDeleted() ? Comment.ADMIN_DELETED_MESSAGE : comment.getContent(),
                comment.getCreatedAt(),
                comment.isAccepted(),
                comment.getAuthorId().equals(currentMemberId),
                comment.isDeleted(),
                fileStoragePort.presignUrl(comment.getImageUrl()),
                replies);
    }
}