package com.wanted.backend.domain.notice.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * [Infrastructure Layer - Spring Data JPA Repository]
 */
public interface SpringDataNoticeRepository
        extends JpaRepository<NoticeJpaEntity, Long> {

    // 전체 공지 목록 (키워드 검색 + 페이징)
    Page<NoticeJpaEntity> findByTypeAndTitleContaining(
            String type, String keyword, Pageable pageable);

    // 강의 공지 목록 (courseId + 키워드 검색 + 페이징)
    Page<NoticeJpaEntity> findByCourseIdAndTypeAndTitleContaining(
            Long courseId, String type, String keyword, Pageable pageable);

    // 이전 공지 조회 - 강의 공지용 (courseId 있을 때)
    Optional<NoticeJpaEntity> findFirstByIdLessThanAndTypeAndCourseIdOrderByIdDesc(
            Long id, String type, Long courseId);

    // 이전 공지 조회 - 전체 공지용 (courseId = null)
    Optional<NoticeJpaEntity> findFirstByIdLessThanAndTypeOrderByIdDesc(
            Long id, String type);

    // INSTRUCTOR/STUDENT용 - 특정 강의 ID 목록으로 조회
    Page<NoticeJpaEntity> findByCourseIdInAndTypeAndTitleContaining(
            List<Long> courseIds, String type, String keyword, Pageable pageable);
}