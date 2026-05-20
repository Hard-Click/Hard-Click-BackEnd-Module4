package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.commend.CreateReviewCommend;

public interface ReviewCommendUseCase {

    Long handle(CreateReviewCommend commend);

}
