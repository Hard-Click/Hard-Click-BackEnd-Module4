package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.domain.identity.application.usecase.ProfileQueryUseCase;
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
public class ProfileQueryService implements ProfileQueryUseCase {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "/images/default-profile.png";

    private final MemberRepository memberRepository;
    private final ProfileImageStoragePort profileImageStoragePort;

    @Override
    public MyProfileView handle(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new MyProfileView(
                member.getId(),
                member.getName(),
                member.getEmail(),
                resolveProfileImageUrl(member)
        );
    }

    private String resolveProfileImageUrl(Member member) {
        if (member.getProfileImageS3Key() != null) {
            return profileImageStoragePort.generatePresignedUrl(member.getProfileImageS3Key());
        }
        if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isBlank()) {
            return member.getProfileImageUrl();
        }
        return DEFAULT_PROFILE_IMAGE_URL;
    }
}
