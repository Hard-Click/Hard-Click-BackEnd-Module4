package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.command.DeletePostCommand;

public interface PostCommandUseCase {
    Long create(CreatePostCommand command);
    void delete(DeletePostCommand command);
}