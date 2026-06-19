package com.wanted.backend.domain.notification;

import com.wanted.backend.domain.notification.application.service.NotificationQueryService;
import com.wanted.backend.domain.notification.domain.model.Notification;
import com.wanted.backend.domain.notification.domain.model.NotificationType;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
import com.wanted.backend.domain.notification.presentation.response.NotificationListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    private final Long memberId = 1L;

    @Test
    @DisplayName("미확인 알림 개수 조회 성공")
    void getUnreadCount_success() {
        // given
        when(notificationRepository.countUnreadByReceiverId(memberId)).thenReturn(3);

        // when
        int count = notificationQueryService.getUnreadCount(memberId);

        // then
        assertThat(count).isEqualTo(3);
        verify(notificationRepository, times(1)).countUnreadByReceiverId(memberId);
    }

    @Test
    @DisplayName("알림이 없으면 미확인 개수 0 반환")
    void getUnreadCount_zero() {
        // given
        when(notificationRepository.countUnreadByReceiverId(memberId)).thenReturn(0);

        // when
        int count = notificationQueryService.getUnreadCount(memberId);

        // then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 첫 요청 (cursorId 없음)")
    void getList_success_firstRequest() {
        // given
        List<Notification> notifications = List.of(
                makeNotification(10L),
                makeNotification(9L)
        );
        when(notificationRepository.findByReceiverIdWithCursor(memberId, null, 10))
                .thenReturn(notifications);
        when(notificationRepository.existsNextPage(memberId, 9L)).thenReturn(false);

        // when
        NotificationListResponse response = notificationQueryService.getList(memberId, null, 10);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - 다음 페이지 존재")
    void getList_success_hasNext() {
        // given
        List<Notification> notifications = List.of(
                makeNotification(10L),
                makeNotification(9L)
        );
        when(notificationRepository.findByReceiverIdWithCursor(memberId, null, 10))
                .thenReturn(notifications);
        when(notificationRepository.existsNextPage(memberId, 9L)).thenReturn(true);

        // when
        NotificationListResponse response = notificationQueryService.getList(memberId, null, 10);

        // then
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("알림 목록 조회 성공 - cursorId 이후 조회")
    void getList_success_withCursor() {
        // given
        List<Notification> notifications = List.of(makeNotification(8L));
        when(notificationRepository.findByReceiverIdWithCursor(memberId, 9L, 10))
                .thenReturn(notifications);
        when(notificationRepository.existsNextPage(memberId, 8L)).thenReturn(false);

        // when
        NotificationListResponse response = notificationQueryService.getList(memberId, 9L, 10);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).notiId()).isEqualTo(8L);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("알림이 없으면 빈 목록과 hasNext=false 반환")
    void getList_empty() {
        // given
        when(notificationRepository.findByReceiverIdWithCursor(memberId, null, 10))
                .thenReturn(Collections.emptyList());

        // when
        NotificationListResponse response = notificationQueryService.getList(memberId, null, 10);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        verify(notificationRepository, never()).existsNextPage(any(), any());
    }

    private Notification makeNotification(Long id) {
        return Notification.restore(
                id, memberId, NotificationType.NOTICE,
                "새로운 공지사항이 등록되었습니다.",
                false, "/notices/1", LocalDateTime.now()
        );
    }
}
