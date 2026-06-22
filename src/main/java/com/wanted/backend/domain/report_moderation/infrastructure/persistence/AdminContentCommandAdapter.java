package com.wanted.backend.domain.report_moderation.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentCommandPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminContentCommandAdapter implements AdminContentCommandPort {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void deleteByAdmin(TargetType contentType, Long contentId) {
        switch (contentType) {
            case POST -> postRepository.adminDeleteById(contentId);
            case COMMENT -> commentRepository.softDeleteByAdmin(contentId, LocalDateTime.now());
            case REVIEW -> deleteReview(contentId);
        }
    }

    private void deleteReview(Long contentId) {
        if (reviewRepository.findById(contentId).isEmpty()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        reviewRepository.adminDeleteById(contentId);
    }
}
