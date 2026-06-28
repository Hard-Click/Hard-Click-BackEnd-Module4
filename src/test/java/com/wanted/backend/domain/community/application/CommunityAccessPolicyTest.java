package com.wanted.backend.domain.community.application;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.MemberAccessPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CommunityAccessPolicyTest {

    @InjectMocks
    private CommunityAccessPolicy communityAccessPolicy;

    @Mock
    private MemberAccessPort memberAccessPort;

    @Test
    @DisplayName("정지/탈퇴 회원은 커뮤니티 접근 시 예외가 발생한다")
    void validateAccess_fail_whenSuspendedOrWithdrawn() {
        // given
        Long memberId = 1L;
        given(memberAccessPort.isSuspendedOrWithdrawn(memberId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> communityAccessPolicy.validateAccess(memberId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.COMMUNITY_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("활성 회원은 커뮤니티 접근이 허용된다")
    void validateAccess_success_whenActive() {
        // given
        Long memberId = 1L;
        given(memberAccessPort.isSuspendedOrWithdrawn(memberId)).willReturn(false);

        // when & then
        assertThatCode(() -> communityAccessPolicy.validateAccess(memberId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("조회 검사: 비로그인(null)은 회원 상태 조회 없이 통과한다")
    void validateAccessIfLoggedIn_skip_whenNull() {
        // when & then
        assertThatCode(() -> communityAccessPolicy.validateAccessIfLoggedIn(null))
                .doesNotThrowAnyException();
        verifyNoInteractions(memberAccessPort);
    }

    @Test
    @DisplayName("조회 검사: 비회원(-1)은 회원 상태 조회 없이 통과한다")
    void validateAccessIfLoggedIn_skip_whenAnonymous() {
        // when & then
        assertThatCode(() -> communityAccessPolicy.validateAccessIfLoggedIn(-1L))
                .doesNotThrowAnyException();
        verifyNoInteractions(memberAccessPort);
    }

    @Test
    @DisplayName("조회 검사: 로그인한 정지/탈퇴 회원은 조회 시에도 차단된다")
    void validateAccessIfLoggedIn_fail_whenSuspended() {
        // given
        Long memberId = 1L;
        given(memberAccessPort.isSuspendedOrWithdrawn(memberId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> communityAccessPolicy.validateAccessIfLoggedIn(memberId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.COMMUNITY_ACCESS_DENIED.getMessage());
    }
}
