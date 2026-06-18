package com.wanted.backend.domain.identity.application.usecase;

import com.wanted.backend.domain.identity.application.command.ChangeMemberStatusCommand;
import com.wanted.backend.domain.identity.application.dto.ChangeMemberStatusResult;

public interface ChangeMemberStatusUseCase {
    ChangeMemberStatusResult changeStatus(ChangeMemberStatusCommand command);
}
