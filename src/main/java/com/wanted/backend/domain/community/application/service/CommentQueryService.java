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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        // 1. 부모 댓글 전체 조회 (1 query)
        List<Comment> parentComments = commentRepository.findByPostIdAndParentIdIsNull(postId);
        if (parentComments.isEmpty()) {
            return new CommentListResult(0, List.of());
        }

        // 2. 대댓글 일괄 조회 (1 IN query — parent_id 인덱스 필요)
        List<Long> parentIds = parentComments.stream().map(Comment::getId).toList();
        Map<Long, List<Comment>> repliesByParentId = commentRepository.findByParentIdIn(parentIds)
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 3. 모든 authorId 수집 후 이름 일괄 조회 (1 IN query)
        Set<Long> allAuthorIds = new HashSet<>();
        parentComments.stream().filter(c -> !c.isDeleted()).forEach(c -> allAuthorIds.add(c.getAuthorId()));
        repliesByParentId.values().forEach(replies ->
                replies.stream().filter(r -> !r.isDeleted()).forEach(r -> allAuthorIds.add(r.getAuthorId())));
        Map<Long, String> nameMap = memberNamePort.getNamesByMemberIds(allAuthorIds);

        // 4. 메모리에서 조립
        List<CommentResult> comments = parentComments.stream()
                .sorted(Comparator
                        .comparing(Comment::isAccepted).reversed()
                        .thenComparing(Comparator.comparing(Comment::getCreatedAt).reversed()))
                .map(comment -> toResult(comment, memberId, isAdmin,
                        repliesByParentId.getOrDefault(comment.getId(), List.of()), nameMap))
                .toList();

        return new CommentListResult(commentRepository.countByPostId(postId), comments);
    }

    private CommentResult toResult(Comment comment, Long currentMemberId, boolean isAdmin,
                                   List<Comment> replies, Map<Long, String> nameMap) {
        String rawName = comment.isDeleted() ? null : nameMap.get(comment.getAuthorId());
        boolean hasName = rawName != null && !rawName.isEmpty();
        String authorName = hasName ? (isAdmin ? rawName : Review.maskName(rawName)) : "";
        String authorInitial = hasName ? rawName.substring(0, 1) : "";

        List<CommentResult> replyResults = replies.stream()
                .map(reply -> toResult(reply, currentMemberId, isAdmin, List.of(), nameMap))
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
                replyResults);
    }
}
