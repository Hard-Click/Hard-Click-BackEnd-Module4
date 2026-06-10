package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.command.DeleteCommentCommand;
import com.wanted.backend.domain.community.application.command.UpdateCommentCommand;


public interface CommentCommandUseCase {

    Long create(CreateCommentCommand command);
    void accept(AcceptCommentCommand command);
    void update(UpdateCommentCommand command);  // 추가
    void delete(DeleteCommentCommand command);

}