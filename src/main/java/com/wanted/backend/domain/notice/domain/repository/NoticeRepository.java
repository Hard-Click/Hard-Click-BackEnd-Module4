package com.wanted.backend.domain.notice.domain.repository;

import com.wanted.backend.domain.notice.domain.model.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface NoticeRepository {

    Notice save(Notice notice);

    // 전체 공지 목록 조회 (키워드 검색 + 페이징)
    Page<Notice> findGlobalNotices(String keyword, Pageable pageable);

    // 강의 공지 목록 조회 (courseId + 키워드 검색 + 페이징)
    Page<Notice> findCourseNotices(Long courseId, String keyword, Pageable pageable);

    // 단건 조회
    Optional<Notice> findById(Long noticeId);

    // 이전 공지 조회
    Optional<Notice> findPreviousNotice(Long noticeId, String type, Long courseId);

    // Hard Delete 추가
    void deleteById(Long noticeId);
}