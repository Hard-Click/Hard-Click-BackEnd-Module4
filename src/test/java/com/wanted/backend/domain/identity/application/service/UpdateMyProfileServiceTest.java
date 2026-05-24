package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.domain.identity.application.usecase.UpdateMyProfileUseCase.MyProfileUpdateView;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateMyProfileServiceTest {

    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private ProfileImageStoragePort profileImageStoragePort;
    private UpdateMyProfileService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        profileImageStoragePort = mock(ProfileImageStoragePort.class);
        service = new UpdateMyProfileService(memberRepository, passwordEncoder, profileImageStoragePort);
    }

    @Test
    void updatesProfileImage() {
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage", "profile.png", "image/png", "image".getBytes()
        );
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(profileImageStoragePort.store(profileImage)).thenReturn("/uploads/profile-images/profile.png");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MyProfileUpdateView result = service.handle(new UpdateMyProfileCommand(
                1L,
                profileImage,
                null,
                null,
                null
        ));

        verify(memberRepository).save(member);
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.profileImageUrl()).isEqualTo("/uploads/profile-images/profile.png");
    }

    @Test
    void updatesPasswordWhenCurrentPasswordMatches() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("currentPassword1!", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1!")).thenReturn("new-encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                "currentPassword1!",
                "newPassword1!",
                "newPassword1!"
        ));

        assertThat(member.getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    void throwsPasswordMismatchWhenCurrentPasswordDoesNotMatch() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword1!", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                "wrongPassword1!",
                "newPassword1!",
                "newPassword1!"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    void throwsInvalidInputWhenPasswordChangeRequestIsPartial() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                null,
                "newPassword1!",
                "newPassword1!"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    void throwsInvalidInputWhenNewPasswordConfirmDoesNotMatch() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                "currentPassword1!",
                "newPassword1!",
                "differentPassword1!"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
    }

    @Test
    void throwsInvalidInputWhenNoUpdatableValueExists() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    void throwsUserNotFoundWhenMemberDoesNotExist() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new UpdateMyProfileCommand(
                1L,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private Member member() {
        LocalDateTime now = LocalDateTime.now();
        return Member.restore(
                1L,
                "testuser",
                "test@example.com",
                "encoded-password",
                "기존이름",
                "MALE",
                LocalDate.of(2000, 1, 1),
                "010-1234-5678",
                "/images/default-profile.png",
                Role.STUDENT,
                MemberStatus.ACTIVE,
                false,
                0,
                false,
                null,
                null,
                now,
                now
        );
    }
}
