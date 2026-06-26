package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.application.port.CourseInfoPort;
import com.wanted.backend.domain.notice.application.port.EnrolledCoursePort;
import com.wanted.backend.domain.notice.application.port.InstructorCoursePort;
import com.wanted.backend.domain.notice.application.usecase.NoticeQueryUseCase;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import com.wanted.backend.domain.notice.presentation.response.NoticeDetailResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeItemResponse;
import com.wanted.backend.domain.notice.presentation.response.NoticeListResponse;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository; 
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
    private final InstructorCoursePort instructorCoursePort;
    private final EnrolledCoursePort enrolledCoursePort;
    private final NotificationRepository notificationRepository; 

    public NoticeQueryService(NoticeRepository noticeRepository,
                              CourseInfoPort courseInfoPort,
                              InstructorCoursePort instructorCoursePort,
                              EnrolledCoursePort enrolledCoursePort,
                              NotificationRepository notificationRepository) {
        this.noticeRepository = noticeRepository;
        this.courseInfoPort = courseInfoPort;
        this.instructorCoursePort = instructorCoursePort;
        this.enrolledCoursePort = enrolledCoursePort;
        this.notificationRepository = notificationRepository; 
    }

    @Override
    public NoticeListResponse getList(GetNoticeListCommand command) {

        Pageable pageable = PageRequest.of(
                command.page(),
                command.size(),
                Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"))
        );

        Page<Notice> noticePage;
        String courseName = null;

        if ("GLOBAL".equals(command.type())) {
            noticePage = noticeRepository.findGlobalNotices(
                    command.keyword() != null ? command.keyword() : "", pageable);

        } else if ("COURSE".equals(command.type())) {
            String role = command.role();

            if ("ADMIN".equals(role)) {
                if (command.courseId() == null) {
                    noticePage = noticeRepository.findAllCourseNotices(
                            command.keyword() != null ? command.keyword() : "", pageable);
                } else {
                    courseName = courseInfoPort.getCourseNameByCourseId(command.courseId());
                    noticePage = noticeRepository.findCourseNotices(
                            command.courseId(), command.keyword() != null ? command.keyword() : "", pageable);
                }

            } else if ("INSTRUCTOR".equals(role)) {
                List<Long> myCourseIds = instructorCoursePort.getCourseIdsByInstructorId(command.memberId());
                noticePage = noticeRepository.findCourseNoticesByIds(
                        myCourseIds, command.keyword() != null ? command.keyword() : "", pageable);

            } else {
                List<Long> enrolledIds = enrolledCoursePort.getEnrolledCourseIdsByMemberId(command.memberId());
                noticePage = noticeRepository.findCourseNoticesByIds(
                        enrolledIds, command.keyword() != null ? command.keyword() : "", pageable);
            }

        } else {
            throw new BusinessException(ErrorCode.INVALID_NOTICE_TYPE);
        }

        // N+1 방지를 위해 페이지 내 공지 ID를 배치로 조회하여 읽음 목록 확보
        List<Long> noticeIds = noticePage.getContent().stream()
                .map(Notice::getId)
                .toList();
        List<Long> readIds = notificationRepository.findReadNoticeIds(command.memberId(), noticeIds);

        final String finalCourseName = courseName;
        List<NoticeItemResponse> content = noticePage.getContent().stream()
                .map(notice -> {
                    String name = finalCourseName != null
                            ? finalCourseName
                            : getCourseNameSafe(notice.getCourseId());
                    boolean isRead = readIds.contains(notice.getId());
                    return toItemResponse(notice, name, isRead);
                })
                .toList();

        return new NoticeListResponse(content, noticePage.getTotalPages());
    }

    @Override
    public NoticeDetailResponse getDetail(Long noticeId, Long memberId) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        // 비로그인 접근 방어 및 단건 읽음 여부 확인
        boolean isRead = memberId != null && notificationRepository.isNoticeRead(memberId, noticeId);

        String courseName = "COURSE".equals(notice.getType())
                ? courseInfoPort.getCourseNameByCourseId(notice.getCourseId())
                : null;

        NoticeDetailResponse.PreviousNotice previousNotice = noticeRepository
                .findPreviousNotice(noticeId, notice.getType(), notice.getCourseId())
                .map(prev -> new NoticeDetailResponse.PreviousNotice(prev.getId(), prev.getTitle()))
                .orElse(null);

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getType(),
                courseName,
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                isRead,
                notice.getCreatedAt(),
                previousNotice
        );
    }

    // isRead 파라미터 추가로 하드코딩 제거
    private NoticeItemResponse toItemResponse(Notice notice, String courseName, boolean isRead) {
        return new NoticeItemResponse(
                notice.getId(),
                notice.getType(),
                courseName,
                notice.getTitle(),
                notice.isPinned(),
                isRead,
                notice.getCreatedAt()
        );
    }

    private String getCourseNameSafe(Long courseId) {
        if (courseId == null) return null;
        try {
            return courseInfoPort.getCourseNameByCourseId(courseId);
        } catch (BusinessException e) {
            return null;
        }
    }
}