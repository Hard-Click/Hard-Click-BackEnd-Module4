package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreateReviewCommand;
import com.wanted.backend.domain.community.application.command.UpdateReviewCommand;

public interface ReviewCommandUseCase {
    Long handle(CreateReviewCommand command);
    Long update(UpdateReviewCommand command);
}