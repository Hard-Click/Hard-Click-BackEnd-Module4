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
}
