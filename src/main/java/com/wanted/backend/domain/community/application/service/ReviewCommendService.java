package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.commend.CreateReviewCommend;
import com.wanted.backend.domain.community.application.usecase.ReviewCommendUseCase;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommendService implements ReviewCommendUseCase {


    private final ReviewRepository reviewRepository;

    @Override
    public Long handle(CreateReviewCommend commend) {
        return 0L;
    }
}
