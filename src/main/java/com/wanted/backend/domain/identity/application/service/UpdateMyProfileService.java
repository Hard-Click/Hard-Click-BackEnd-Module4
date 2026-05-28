package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.domain.identity.application.usecase.UpdateMyProfileUseCase;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.policy.PasswordPolicy;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStoragePort profileImageStoragePort;

    @Override
    public MyProfileUpdateView handle(UpdateMyProfileCommand command) {
        // 인증된 사용자 기준으로 수정 대상 회원을 먼저 조회
        // 이후 검증/파일 저장/비밀번호 인코딩이 끝난 뒤 도메인 모델에 변경을 위임
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // UI에서는 변경값이 없으면 저장 버튼이 비활성화되는 흐름
        // 백엔드도 빈 multipart 요청이나 잘못된 직접 호출을 막기 위해 동일한 기준으로 검증
        validateHasUpdatableValue(command);

        String profileImageUrl = storeProfileImage(command.profileImage());
        String encodedPassword = resolveEncodedPassword(command, member);

        member.updateProfile(profileImageUrl, encodedPassword, LocalDateTime.now());
        Member saved = memberRepository.save(member);

        return new MyProfileUpdateView(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getProfileImageUrl()
        );
    }

    private String storeProfileImage(MultipartFile profileImage) {
        // 이미지가 첨부되지 않은 요청은 프로필 이미지 변경 없이 그대로 유지
        // 실제 저장과 확장자/용량 검증은 스토리지 포트 구현체에서 처리
        if (profileImage == null || profileImage.isEmpty()) {
            return null;
        }
        return profileImageStoragePort.store(profileImage);
    }

    private void validateHasUpdatableValue(UpdateMyProfileCommand command) {
        // 프로필 수정 API는 이미지 변경, 비밀번호 변경, 또는 둘 다 변경하는 세 흐름을 지원
        // 둘 중 아무 입력도 없다면 수정할 내용이 없는 요청으로 보고 실패
        boolean hasProfileImage = command.profileImage() != null && !command.profileImage().isEmpty();
        boolean hasPasswordInput = StringUtils.hasText(command.currentPassword())
                || StringUtils.hasText(command.newPassword())
                || StringUtils.hasText(command.newPasswordConfirm());

        if (!hasProfileImage && !hasPasswordInput) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String resolveEncodedPassword(UpdateMyProfileCommand command, Member member) {
        // 비밀번호 입력이 전혀 없으면 비밀번호 변경을 하지 않는 요청
        // 하나라도 입력했다면 현재 비밀번호, 새 비밀번호, 확인값이 모두 필요
        boolean hasCurrentPassword = StringUtils.hasText(command.currentPassword());
        boolean hasNewPassword = StringUtils.hasText(command.newPassword());
        boolean hasNewPasswordConfirm = StringUtils.hasText(command.newPasswordConfirm());

        if (!hasCurrentPassword && !hasNewPassword && !hasNewPasswordConfirm) {
            return null;
        }
        if (!hasCurrentPassword || !hasNewPassword || !hasNewPasswordConfirm) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // 프론트의 확인 입력값과 서버 검증을 모두 둬서 잘못된 직접 호출도 방어
        if (!command.newPassword().equals(command.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        // 회원가입과 동일한 비밀번호 정책을 사용해 정책 불일치를 방지
        if (!PasswordPolicy.isValid(command.newPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // 현재 비밀번호가 맞는 경우에만 새 비밀번호로 교체
        if (!passwordEncoder.matches(command.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        return passwordEncoder.encode(command.newPassword());
    }
}
