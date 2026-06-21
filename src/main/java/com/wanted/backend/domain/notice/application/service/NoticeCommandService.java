package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.CreateGlobalNoticeCommand;
import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;
import com.wanted.backend.domain.notice.application.command.DeleteNoticeCommand;
import com.wanted.backend.domain.notice.application.command.UpdateNoticeCommand;
import com.wanted.backend.domain.notice.application.policy.GlobalNoticeCreatePolicy;
import com.wanted.backend.domain.notice.application.policy.NoticeCreatePolicy;
import com.wanted.backend.domain.notice.application.policy.NoticeUpdatePolicy;
import com.wanted.backend.domain.notice.application.port.AdminValidationPort;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.domain.event.NoticeCreatedEvent;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCreatePolicy noticeCreatePolicy;
    private final GlobalNoticeCreatePolicy globalNoticeCreatePolicy;
    private final NoticeUpdatePolicy noticeUpdatePolicy;
    private final AdminValidationPort adminValidationPort;
    private final ApplicationEventPublisher eventPublisher;

    public NoticeCommandService(NoticeRepository noticeRepository,
                                NoticeCreatePolicy noticeCreatePolicy,
                                GlobalNoticeCreatePolicy globalNoticeCreatePolicy,
                                NoticeUpdatePolicy noticeUpdatePolicy,
                                AdminValidationPort adminValidationPort,
                                ApplicationEventPublisher eventPublisher) {
        this.noticeRepository = noticeRepository;
        this.noticeCreatePolicy = noticeCreatePolicy;
        this.globalNoticeCreatePolicy = globalNoticeCreatePolicy;
        this.noticeUpdatePolicy = noticeUpdatePolicy;
        this.adminValidationPort = adminValidationPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long create(CreateNoticeCommand command) {
        noticeCreatePolicy.validate(command.instructorId(), command.courseId());

        Notice notice = Notice.create(command.instructorId(), command.courseId(),
                command.title(), command.content(), command.isPinned());
        Notice saved = noticeRepository.save(notice);

        boolean isAdmin = adminValidationPort.isAdmin(command.instructorId());
        eventPublisher.publishEvent(NoticeCreatedEvent.of(
                saved.getId(), saved.getCourseId(), "COURSE", saved.getTitle(), isAdmin));

        return saved.getId();
    }

    @Override
    public Long createGlobal(CreateGlobalNoticeCommand command) {
        globalNoticeCreatePolicy.validate(command.adminId());

        Notice notice = Notice.createGlobal(command.adminId(),
                command.title(), command.content(), command.isPinned());
        Notice saved = noticeRepository.save(notice);

        eventPublisher.publishEvent(NoticeCreatedEvent.of(
                saved.getId(), null, "GLOBAL", saved.getTitle(), true));

        return saved.getId();
    }

    @Override
    public void update(UpdateNoticeCommand command) {
        Notice notice = noticeRepository.findById(command.noticeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        noticeUpdatePolicy.validate(command.memberId(), notice);
        notice.update(command.title(), command.content(), command.isPinned());
        noticeRepository.save(notice);
    }

    @Override
    public void delete(DeleteNoticeCommand command) {
        Notice notice = noticeRepository.findById(command.noticeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        noticeUpdatePolicy.validate(command.memberId(), notice);
        noticeRepository.deleteById(command.noticeId());
    }
}