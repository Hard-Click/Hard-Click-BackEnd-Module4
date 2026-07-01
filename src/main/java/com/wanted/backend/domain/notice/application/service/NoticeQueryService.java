package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.GetNoticeListCommand;
import com.wanted.backend.domain.notice.application.port.CourseInfoPort;
import com.wanted.backend.domain.notice.application.port.EnrolledCoursePort;
import com.wanted.backend.domain.notice.application.port.InstructorCoursePort;
import com.wanted.backend.domain.notice.application.result.NoticeDetailResult;
import com.wanted.backend.domain.notice.application.result.NoticeItemResult;
import com.wanted.backend.domain.notice.application.result.NoticeListResult;
import com.wanted.backend.domain.notice.application.usecase.NoticeQueryUseCase;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
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
import java.util.Map;
import java.util.Objects;

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
    public NoticeListResult getList(GetNoticeListCommand command) {

        Pageable pageable = PageRequest.of(
                command.page(), command.size(),
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
                if (command.courseId() != null) {
                    if (!myCourseIds.contains(command.courseId())) {
                        throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
                    }
                    courseName = courseInfoPort.getCourseNameByCourseId(command.courseId());
                    noticePage = noticeRepository.findCourseNotices(
                            command.courseId(), command.keyword() != null ? command.keyword() : "", pageable);
                } else {
                    noticePage = noticeRepository.findCourseNoticesByIds(
                            myCourseIds, command.keyword() != null ? command.keyword() : "", pageable);
                }
            } else {
                List<Long> enrolledIds = enrolledCoursePort.getEnrolledCourseIdsByMemberId(command.memberId());
                if (command.courseId() != null) {
                    if (!enrolledIds.contains(command.courseId())) {
                        throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
                    }
                    courseName = courseInfoPort.getCourseNameByCourseId(command.courseId());
                    noticePage = noticeRepository.findCourseNotices(
                            command.courseId(), command.keyword() != null ? command.keyword() : "", pageable);
                } else {
                    noticePage = noticeRepository.findCourseNoticesByIds(
                            enrolledIds, command.keyword() != null ? command.keyword() : "", pageable);
                }
            }

        } else {
            throw new BusinessException(ErrorCode.INVALID_NOTICE_TYPE);
        }

        List<Long> noticeIds = noticePage.getContent().stream()
                .map(Notice::getId)
                .toList();

        if (noticeIds.isEmpty()) {
            return new NoticeListResult(List.of(), noticePage.getTotalPages());
        }

        List<Long> readIds = notificationRepository.findReadNoticeIds(command.memberId(), noticeIds);

        final String finalCourseName = courseName;
        Map<Long, String> courseNameMap = Map.of();
        if ("COURSE".equals(command.type()) && finalCourseName == null) {
            List<Long> courseIds = noticePage.getContent().stream()
                    .map(Notice::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            courseNameMap = courseInfoPort.getCourseNamesByCourseIds(courseIds);
        }

        final Map<Long, String> finalCourseNameMap = courseNameMap;
        List<NoticeItemResult> content = noticePage.getContent().stream()
                .map(notice -> {
                    String name = finalCourseName != null
                            ? finalCourseName
                            : notice.getCourseId() != null
                            ? finalCourseNameMap.get(notice.getCourseId())
                            : null;
                    boolean isRead = readIds.contains(notice.getId());
                    return new NoticeItemResult(
                            notice.getId(), notice.getType(), name,
                            notice.getTitle(), notice.isPinned(), isRead, notice.getCreatedAt());
                })
                .toList();

        return new NoticeListResult(content, noticePage.getTotalPages());
    }

    @Override
    public NoticeDetailResult getDetail(Long noticeId, Long memberId, String role) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        if ("COURSE".equals(notice.getType())) {
            validateCourseNoticeAccess(notice.getCourseId(), memberId, role);
        }

        boolean isRead = notificationRepository.isNoticeRead(memberId, noticeId);

        String courseName = "COURSE".equals(notice.getType())
                ? courseInfoPort.getCourseNameByCourseId(notice.getCourseId())
                : null;

        NoticeDetailResult.PreviousNoticeResult previousNotice = noticeRepository
                .findPreviousNotice(noticeId, notice.getType(), notice.getCourseId())
                .map(prev -> new NoticeDetailResult.PreviousNoticeResult(prev.getId(), prev.getTitle()))
                .orElse(null);

        return new NoticeDetailResult(
                notice.getId(), notice.getType(), courseName, notice.getTitle(),
                notice.getContent(), notice.isPinned(), isRead, notice.getCreatedAt(),
                previousNotice);
    }
    private void validateCourseNoticeAccess(Long courseId, Long memberId, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if ("INSTRUCTOR".equals(role)) {
            if (!instructorCoursePort.getCourseIdsByInstructorId(memberId).contains(courseId)) {
                throw new BusinessException(ErrorCode.NOTICE_NOT_AUTHORIZED);
            }
            return;
        }
        if (!enrolledCoursePort.getEnrolledCourseIdsByMemberId(memberId).contains(courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
    }
}