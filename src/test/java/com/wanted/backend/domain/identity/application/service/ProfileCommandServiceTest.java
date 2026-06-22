package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.command.UpdateMyProfileCommand;
import com.wanted.backend.domain.identity.application.command.WithdrawMemberCommand;
import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.domain.identity.application.usecase.ProfileCommandUseCase.MyProfileUpdateView;
import com.wanted.backend.domain.identity.domain.model.Member;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.Role;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.domain.repository.RefreshTokenRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileCommandServiceTest {

    private MemberRepository memberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private ProfileImageStoragePort profileImageStoragePort;
    private ProfileCommandService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        profileImageStoragePort = mock(ProfileImageStoragePort.class);
        service = new ProfileCommandService(
                memberRepository,
                refreshTokenRepository,
                passwordEncoder,
                profileImageStoragePort
        );
    }

    @Test
    void 프로필_이미지를_수정한다() {
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
    void 현재_비밀번호가_일치하면_비밀번호를_수정한다() {
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
    void 현재_비밀번호가_일치하지_않으면_예외가_발생한다() {
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
    void 비밀번호_변경_요청이_일부만_있으면_예외가_발생한다() {
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
    void 새_비밀번호와_확인이_일치하지_않으면_예외가_발생한다() {
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
    void 수정할_값이_없으면_예외가_발생한다() {
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
    void 회원이_존재하지_않으면_예외가_발생한다() {
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

    @Test
    void withdraw_anonymizes_member_personal_data() {
        Member member = member();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("currentPassword1!", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-deleted-password");

        service.withdraw(1L, new WithdrawMemberCommand("currentPassword1!"));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        verify(refreshTokenRepository).deleteByMemberId(1L);

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(savedMember.getEmail())
                .startsWith("withdrawn_1_")
                .endsWith("@deleted.local");
        assertThat(savedMember.getUsername()).startsWith("withdrawn_1_");
        assertThat(savedMember.getName()).isEqualTo("탈퇴회원");
        assertThat(savedMember.getPassword()).isEqualTo("encoded-deleted-password");
        assertThat(savedMember.getPhoneNumber()).isNull();
        assertThat(savedMember.getProfileImageUrl()).isNull();
        assertThat(savedMember.getGender()).isNull();
        assertThat(savedMember.getBirthDate()).isNull();
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
                now,
                false
        );
    }
}
