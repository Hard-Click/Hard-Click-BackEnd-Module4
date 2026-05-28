package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.command.DeletePostCommand;
import com.wanted.backend.domain.community.application.command.UpdatePostCommand;

public interface PostCommandUseCase {
    Long create(CreatePostCommand command);
    void delete(DeletePostCommand command);
    Long update(UpdatePostCommand command);
}