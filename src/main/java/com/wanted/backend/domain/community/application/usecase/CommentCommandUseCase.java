package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.CreateCommentCommand;


public interface CommentCommandUseCase {
    Long create(CreateCommentCommand command);
}