package com.wanted.backend.domain.identity.application.usecase;

public interface GetMyProfileUseCase {

    MyProfileView handle(Long memberId);

    record MyProfileView(
            Long memberId,
            String name,
            String email,
            String profileImageUrl
    ) {
    }
}
