package com.wanted.backend.domain.report_moderation.application;

import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.application.command.ChangeAdminContentStatusCommand;
import com.wanted.backend.domain.report_moderation.application.dto.AdminContentStatusResult;
import com.wanted.backend.domain.report_moderation.application.port.AdminContentCommandPort;
import com.wanted.backend.domain.report_moderation.application.service.AdminContentCommandService;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminContentCommandServiceTest {

    @InjectMocks
    private AdminContentCommandService adminContentCommandService;

    @Mock
    private AdminContentCommandPort adminContentCommandPort;

    @Test
    @DisplayName("ADMIN_DELETED 요청 시 콘텐츠가 관리자 삭제되고 결과를 반환한다")
    void changeStatus_success_adminDeleted() {
        // given
        ChangeAdminContentStatusCommand command = new ChangeAdminContentStatusCommand(
                TargetType.POST, 15L, "ADMIN_DELETED", "운영 정책 위반으로 삭제합니다.");

        // when
        AdminContentStatusResult result = adminContentCommandService.changeStatus(command);

        // then
        verify(adminContentCommandPort).deleteByAdmin(TargetType.POST, 15L);
        assertThat(result.contentType()).isEqualTo(TargetType.POST);
        assertThat(result.contentId()).isEqualTo(15L);
        assertThat(result.status()).isEqualTo("ADMIN_DELETED");
        assertThat(result.memo()).isEqualTo("운영 정책 위반으로 삭제합니다.");
    }

    @Test
    @DisplayName("ADMIN_DELETED 외의 상태값 요청 시 예외가 발생하고 삭제가 호출되지 않는다")
    void changeStatus_fail_invalidStatus() {
        // given
        ChangeAdminContentStatusCommand command = new ChangeAdminContentStatusCommand(
                TargetType.POST, 15L, "ACTIVE", "복구합니다.");

        // when & then
        assertThatThrownBy(() -> adminContentCommandService.changeStatus(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        verify(adminContentCommandPort, never()).deleteByAdmin(any(), anyLong());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("status가 null이거나 비어있으면 예외가 발생하고 삭제가 호출되지 않는다")
    void changeStatus_fail_nullOrBlankStatus(String invalidStatus) {
        // given
        ChangeAdminContentStatusCommand command = new ChangeAdminContentStatusCommand(
                TargetType.POST, 15L, invalidStatus, "메모");

        // when & then
        assertThatThrownBy(() -> adminContentCommandService.changeStatus(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        verify(adminContentCommandPort, never()).deleteByAdmin(any(), anyLong());
    }
}
