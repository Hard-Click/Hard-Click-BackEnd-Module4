package com.wanted.backend.domain.learning_activity.application.command;

// 유스케이스 실행에 필요한 입력값을 담는다
public record VideoPlayCommand(
        // TODO : 인증 구현 시 memberId도 넣는게 좋을듯?
        Long videoId
) {
}
