package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.application.port.CourseInfoPort;
import com.wanted.backend.domain.notice.application.usecase.NoticeQueryUseCase;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import com.wanted.backend.domain.notice.presentation.response.NoticeDetailResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeItemResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeListResponse;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoticeQueryService implements NoticeQueryUseCase {

    private final NoticeRepository noticeRepository;
    private final CourseInfoPort courseInfoPort;

    public NoticeQueryService(NoticeRepository noticeRepository,
                              CourseInfoPort courseInfoPort) {
        this.noticeRepository = noticeRepository;
        this.courseInfoPort = courseInfoPort;
    }

    @Override
    public NoticeListResponse getList(GetNoticeListCommand command) {

        // type=COURSE일 때 courseId 필수 검증
        if ("COURSE".equals(command.type()) && command.courseId() == null) {
            throw new BusinessException(ErrorCode.COURSE_ID_REQUIRED);
        }

        // isPinned 최상단 + 최신순 정렬
        Pageable pageable = PageRequest.of(
                command.page(),
                command.size(),
                Sort.by(
                        Sort.Order.desc("isPinned"),
                        Sort.Order.desc("createdAt")
                )
        );

        // type에 따라 조회 분기
        Page<Notice> noticePage = "GLOBAL".equals(command.type())
                ? noticeRepository.findGlobalNotices(
                command.keyword() != null ? command.keyword() : "", pageable)
                : noticeRepository.findCourseNotices(
                command.courseId(),
                command.keyword() != null ? command.keyword() : "", pageable);

        // 강의명 조회 (COURSE 타입일 때만)
        String courseName = "COURSE".equals(command.type())
                ? courseInfoPort.getCourseNameByCourseId(command.courseId())
                : null;

        List<NoticeItemResponse> content = noticePage.getContent()
                .stream()
                .map(notice -> toItemResponse(notice, courseName))
                .toList();

        return new NoticeListResponse(content, noticePage.getTotalPages());
    }

    @Override
    public NoticeDetailResponse getDetail(Long noticeId) {

        // 공지 존재 여부 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        // 강의명 조회 (COURSE 타입일 때만)
        String courseName = "COURSE".equals(notice.getType())
                ? courseInfoPort.getCourseNameByCourseId(notice.getCourseId())
                : null;

        // 이전 공지 조회
        NoticeDetailResponse.PreviousNotice previousNotice = noticeRepository
                .findPreviousNotice(noticeId, notice.getType(), notice.getCourseId())
                .map(prev -> new NoticeDetailResponse.PreviousNotice(
                        prev.getId(), prev.getTitle()))
                .orElse(null);

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getType(),
                courseName,
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                false,  // isRead: 추후 구현
                notice.getCreatedAt(),
                previousNotice
        );
    }

    private NoticeItemResponse toItemResponse(Notice notice, String courseName) {
        return new NoticeItemResponse(
                notice.getId(),
                notice.getType(),
                courseName,
                notice.getTitle(),
                notice.isPinned(),
                false,  // isRead: 추후 구현
                notice.getCreatedAt()
        );
    }
}