package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;

public interface PostCommandUseCase {
    Long create(CreatePostCommand command);
}