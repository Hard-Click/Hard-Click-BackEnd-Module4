package com.wanted.backend.domain.notice.application.service;

import com.wanted.backend.domain.notice.application.command.CreateNoticeCommand;
import com.wanted.backend.domain.notice.application.policy.NoticeCreatePolicy;
import com.wanted.backend.domain.notice.application.usecase.NoticeCommandUseCase;
import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class NoticeCommandService implements NoticeCommandUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeCreatePolicy noticeCreatePolicy;

    public NoticeCommandService(NoticeRepository noticeRepository,
                                NoticeCreatePolicy noticeCreatePolicy) {
        this.noticeRepository = noticeRepository;
        this.noticeCreatePolicy = noticeCreatePolicy;
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
}