package com.wanted.backend.domain.notice.domain.repository;

import com.wanted.backend.domain.notice.domain.model.Notice;

public interface NoticeRepository {
    Notice save(Notice notice);
}