package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;

public interface ReviewCommandUseCase {

    Long handle(CreateReviewCommand commend);

}
