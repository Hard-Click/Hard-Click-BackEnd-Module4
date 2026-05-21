package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.usecase.ReviewCommandUseCase;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService implements ReviewCommandUseCase {


    private final ReviewRepository reviewRepository;

    @Override
    public Long handle(CreateReviewCommand commend) {

        Review review = Review.create(commend.memberId(), commend.courseId(), commend.rating(), commend.content());
        Review saved = reviewRepository.save(review);
        return saved.getId();
    }
}
