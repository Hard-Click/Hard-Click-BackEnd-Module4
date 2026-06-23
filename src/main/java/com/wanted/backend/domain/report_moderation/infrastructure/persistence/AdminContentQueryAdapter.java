package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminContentQueryAdapter implements AdminContentQueryPort {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Optional<AdminContentResult> findContent(TargetType contentType, Long contentId) {
        return switch (contentType) {
            case POST -> postRepository.findById(contentId).map(this::toResult);
            case COMMENT -> commentRepository.findById(contentId).map(this::toResult);
            case REVIEW -> reviewRepository.findById(contentId).map(this::toResult);
        };
    }

    private AdminContentResult toResult(Post post) {
        return new AdminContentResult(
                TargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus().name()
        );
    }

    private AdminContentResult toResult(Comment comment) {
        return new AdminContentResult(
                TargetType.COMMENT,
                comment.getId(),
                null,
                comment.getContent(),
                comment.getStatus().name()
        );
    }

    private AdminContentResult toResult(Review review) {
        return new AdminContentResult(
                TargetType.REVIEW,
                review.getId(),
                null,
                review.getContent(),
                review.getStatus().name()
        );
    }
}
