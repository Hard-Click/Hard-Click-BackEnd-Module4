package com.wanted.backend.domain.notification.application.listener;

import com.wanted.backend.domain.community.domain.event.CommentAcceptedEvent;
import com.wanted.backend.domain.community.domain.event.CommentReplyCreatedEvent;
import com.wanted.backend.domain.community.domain.event.PostCommentCreatedEvent;
import com.wanted.backend.domain.community.domain.event.ReportCreatedEvent;
import com.wanted.backend.domain.cource.domain.event.CourseCreatedEvent;
import com.wanted.backend.domain.notice.domain.event.NoticeCreatedEvent;
import com.wanted.backend.domain.notification.application.port.CourseEnrolleeQueryPort;
import com.wanted.backend.domain.notification.application.port.MemberIdQueryPort;
import com.wanted.backend.domain.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.backend.domain.notification.domain.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
public class NotificationEventListener {

    private final NotificationCommandUseCase notificationCommandUseCase;
    private final CourseEnrolleeQueryPort courseEnrolleeQueryPort;
    private final MemberIdQueryPort memberIdQueryPort;

    public NotificationEventListener(NotificationCommandUseCase notificationCommandUseCase,
                                     CourseEnrolleeQueryPort courseEnrolleeQueryPort,
                                     MemberIdQueryPort memberIdQueryPort) {
        this.notificationCommandUseCase = notificationCommandUseCase;
        this.courseEnrolleeQueryPort = courseEnrolleeQueryPort;
        this.memberIdQueryPort = memberIdQueryPort;
    }

    // ── 커뮤니티 ──────────────────────────────────────────────────

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostComment(PostCommentCreatedEvent event) {
        if (event.postAuthorId().equals(event.commentAuthorId())) return;

        notificationCommandUseCase.send(
                event.postAuthorId(),
                NotificationType.POST_COMMENT,
                "내 게시글에 새 댓글이 달렸습니다.",
                "/posts/" + event.postId()
        );
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentReply(CommentReplyCreatedEvent event) {
        if (event.parentCommentAuthorId().equals(event.replyAuthorId())) return;

        notificationCommandUseCase.send(
                event.parentCommentAuthorId(),
                NotificationType.COMMENT_REPLY,
                "내 댓글에 대댓글이 달렸습니다.",
                "/posts/" + event.postId()
        );
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentAccepted(CommentAcceptedEvent event) {
        notificationCommandUseCase.send(
                event.commentAuthorId(),
                NotificationType.COMMENT_ACCEPTED,
                "내 댓글이 채택되었습니다.",
                "/posts/" + event.postId()
        );
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportCreated(ReportCreatedEvent event) {
        List<Long> adminIds = memberIdQueryPort.findAllAdminIds();
        for (Long adminId : adminIds) {
            notificationCommandUseCase.send(
                    adminId,
                    NotificationType.REPORT,
                    "새로운 신고가 접수되었습니다.",
                    "/admin/reports/" + event.reportId()
            );
        }
    }

    // ── 공지 ──────────────────────────────────────────────────────

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNoticeCreated(NoticeCreatedEvent event) {
        if ("GLOBAL".equals(event.type())) {
            // 관리자 전체 공지 → 강사 전원 + 학생 전원
            String message = "새로운 전체 공지가 등록되었습니다: " + event.title();
            String url = "/notices/" + event.noticeId();

            for (Long id : memberIdQueryPort.findAllInstructorIds()) {
                notificationCommandUseCase.send(id, NotificationType.NOTICE, message, url);
            }
            for (Long id : memberIdQueryPort.findAllStudentIds()) {
                notificationCommandUseCase.send(id, NotificationType.NOTICE, message, url);
            }

        } else {
            // 강의 공지
            String url = "/notices/" + event.noticeId();

            if (!event.createdByAdmin()) {
                // 강사 작성 → 관리자에게도 알림
                for (Long adminId : memberIdQueryPort.findAllAdminIds()) {
                    notificationCommandUseCase.send(
                            adminId,
                            NotificationType.NOTICE,
                            "강사가 강의 공지를 등록했습니다: " + event.title(),
                            url
                    );
                }
            }

            // 수강생 알림 (강사/관리자 작성 공통)
            List<Long> enrolledIds = courseEnrolleeQueryPort
                    .findMemberIdsByCourseId(event.courseId());
            for (Long memberId : enrolledIds) {
                notificationCommandUseCase.send(
                        memberId,
                        NotificationType.NOTICE,
                        "수강 중인 강의에 새 공지가 등록되었습니다: " + event.title(),
                        url
                );
            }
        }
    }

    // ── 강좌 개설 ─────────────────────────────────────────────────

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCourseCreated(CourseCreatedEvent event) {
        // 강사 강좌 개설 → 관리자 전원에게 알림
        for (Long adminId : memberIdQueryPort.findAllAdminIds()) {
            notificationCommandUseCase.send(
                    adminId,
                    NotificationType.COURSE_REGISTER,
                    "새로운 강좌가 등록되었습니다: " + event.title(),
                    "/admin/courses/" + event.courseId()
            );
        }
    }
}