package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.CreateGlobalNoticeCommand;
import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;
import com.wanted.backend.domain.notice.application.command.DeleteNoticeCommand;
import com.wanted.backend.domain.notice.application.command.UpdateNoticeCommand;
import com.wanted.backend.domain.notice.application.policy.GlobalNoticeCreatePolicy;
import com.wanted.backend.domain.notice.application.policy.NoticeCreatePolicy;
import com.wanted.backend.domain.notice.application.policy.NoticeUpdatePolicy;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCreatePolicy noticeCreatePolicy;
    private final GlobalNoticeCreatePolicy globalNoticeCreatePolicy;
    private final NoticeUpdatePolicy noticeUpdatePolicy;

    public NoticeCommandService(NoticeRepository noticeRepository,
                                NoticeCreatePolicy noticeCreatePolicy, GlobalNoticeCreatePolicy globalNoticeCreatePolicy, NoticeUpdatePolicy noticeUpdatePolicy) {

        this.noticeRepository = noticeRepository;
        this.noticeCreatePolicy = noticeCreatePolicy;
        this.globalNoticeCreatePolicy = globalNoticeCreatePolicy;
        this.noticeUpdatePolicy = noticeUpdatePolicy;
    }

    @Override
    public Long create(CreateNoticeCommand command) {

        // [1단계] 강사 여부 검증 → Policy에 위임
        noticeCreatePolicy.validate(command.instructorId(), command.courseId());

        // [2단계] 공지사항 도메인 생성 (status PUBLISHED 고정)
        Notice notice = Notice.create(
                command.instructorId(),
                command.courseId(),
                command.title(),
                command.content(),
                command.isPinned()
        );

        // [3단계] DB 저장
        return noticeRepository.save(notice).getId();
    }

    @Override
    public Long createGlobal(CreateGlobalNoticeCommand command) {

        // [1단계] 관리자 여부 검증 → Policy에 위임
        globalNoticeCreatePolicy.validate(command.adminId());

        // [2단계] 전체 공지사항 도메인 생성 (type GLOBAL, courseId null 고정)
        Notice notice = Notice.createGlobal(
                command.adminId(),
                command.title(),
                command.content(),
                command.isPinned()
        );

        // [3단계] DB 저장
        return noticeRepository.save(notice).getId();
    }

    @Override
    public void update(UpdateNoticeCommand command) {

        // [1단계] 공지 존재 여부 확인
        Notice notice = noticeRepository.findById(command.noticeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        // [2단계] 권한 검증 → Policy에 위임
        noticeUpdatePolicy.validate(command.memberId(), notice);

        // [3단계] 공지 수정 → 도메인이 담당
        notice.update(command.title(), command.content(), command.isPinned());

        // [4단계] DB 저장
        noticeRepository.save(notice);
    }

    @Override
    public void delete(DeleteNoticeCommand command) {

        // [1단계] 공지 존재 여부 확인
        Notice notice = noticeRepository.findById(command.noticeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        // [2단계] 권한 검증 → Policy에 위임
        noticeUpdatePolicy.validate(command.memberId(), notice);

        // [3단계] Hard Delete
        noticeRepository.deleteById(command.noticeId());
    }
}