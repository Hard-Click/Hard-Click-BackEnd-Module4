package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.GetMyProfileUseCase;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyProfileService implements GetMyProfileUseCase {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "/images/default-profile.png";

    private final MemberRepository memberRepository;

    @Override
    public MyProfileView handle(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new MyProfileView(
                member.getId(),
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                resolveProfileImageUrl(member.getProfileImageUrl())
        );
    }

    private String resolveProfileImageUrl(String profileImageUrl) {
        return profileImageUrl == null || profileImageUrl.isBlank()
                ? DEFAULT_PROFILE_IMAGE_URL
                : profileImageUrl;
    }
}
