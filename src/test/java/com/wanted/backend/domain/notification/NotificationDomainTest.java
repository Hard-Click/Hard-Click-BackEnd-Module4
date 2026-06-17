package com.wanted.backend.domain.notification;

import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.model.NotificationType;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NotificationDomainTest {

    @Test
    @DisplayName("receiverId가 null이면 알림 생성 실패")
    void create_fail_nullReceiverId() {
        assertThatThrownBy(() -> Notification.create(
                null, NotificationType.NOTICE,
                "새로운 공지사항이 등록되었습니다.", "/notices/1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_NOTIFICATION.getMessage());
    }

    @Test
    @DisplayName("type이 null이면 알림 생성 실패")
    void create_fail_nullType() {
        assertThatThrownBy(() -> Notification.create(
                1L, null,
                "새로운 공지사항이 등록되었습니다.", "/notices/1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_NOTIFICATION.getMessage());
    }

    @Test
    @DisplayName("message가 null이면 알림 생성 실패")
    void create_fail_nullMessage() {
        assertThatThrownBy(() -> Notification.create(
                1L, NotificationType.NOTICE,
                null, "/notices/1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_NOTIFICATION.getMessage());
    }

    @Test
    @DisplayName("message가 공백이면 알림 생성 실패")
    void create_fail_blankMessage() {
        assertThatThrownBy(() -> Notification.create(
                1L, NotificationType.NOTICE,
                "   ", "/notices/1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_NOTIFICATION.getMessage());
    }
}