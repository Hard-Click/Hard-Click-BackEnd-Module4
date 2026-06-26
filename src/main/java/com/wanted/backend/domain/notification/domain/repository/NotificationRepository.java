package com.wanted.backend.domain.notification.domain.repository;

import com.wanted.backend.domain.notification.domain.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    int countUnreadByReceiverId(Long receiverId);
    Optional<Notification> findById(Long notificationId);
    void updateRead(Long notificationId);
    List<Notification> findByReceiverIdWithCursor(Long receiverId, Long cursorId, int size);
    boolean existsNextPage(Long receiverId, Long lastId);
    List<Notification> saveAll(List<Notification> notifications);
    void deleteByRedirectUrlStartingWith(String urlPrefix);

    // 공지 상세 조회 시 단건 읽음 여부 반환
    boolean isNoticeRead(Long memberId, Long noticeId);

    // 공지 목록 조회 시 읽음 처리된 공지 ID 목록 반환
    List<Long> findReadNoticeIds(Long memberId, List<Long> noticeIds);
}