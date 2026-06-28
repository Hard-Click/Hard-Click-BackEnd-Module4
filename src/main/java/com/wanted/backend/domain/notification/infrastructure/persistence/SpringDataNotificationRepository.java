package com.wanted.backend.domain.notification.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataNotificationRepository
        extends JpaRepository<NotificationJpaEntity, Long> {

    int countByReceiverIdAndIsRead(Long receiverId, boolean isRead);

    List<NotificationJpaEntity> findByReceiverIdOrderByIdDesc(
            Long receiverId, Pageable pageable);

    List<NotificationJpaEntity> findByReceiverIdAndIdLessThanOrderByIdDesc(
            Long receiverId, Long cursorId, Pageable pageable);

    boolean existsByReceiverIdAndIdLessThan(Long receiverId, Long lastId);

    void deleteByRedirectUrlStartingWith(String urlPrefix);

    // 특정 공지에 대한 단건 읽음 여부 확인
    boolean existsByReceiverIdAndRedirectUrlAndIsReadTrue(Long receiverId, String redirectUrl);

    // 목록 조회 시 읽음 처리된 알림을 배치로 조회
    List<NotificationJpaEntity> findByReceiverIdAndRedirectUrlInAndIsReadTrue(
            Long receiverId, List<String> redirectUrls);
}