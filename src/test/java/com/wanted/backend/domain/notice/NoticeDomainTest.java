package com.wanted.backend.domain.notice;

import com.wanted.backend.domain.notice.domain.model.Notice;
import com.wanted.backend.domain.notice.domain.model.NoticeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class NoticeDomainTest {

    @Test
    @DisplayName("강의 공지사항 생성 시 type=COURSE, status=PUBLISHED 고정")
    void create_typeAndStatusFixed() {
        // given & when
        Notice notice = Notice.create(
                10L, 1L,
                "3주차 과제 제출 안내",
                "이번 주 일요일까지 제출해주세요.",
                true
        );

        // then
        assertThat(notice.getType()).isEqualTo("COURSE");
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
        assertThat(notice.isPinned()).isTrue();
        assertThat(notice.getId()).isNull();  // 저장 전 id = null
        assertThat(notice.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("강의 공지사항 restore 시 모든 필드 정상 복원")
    void restore_allFieldsRestored() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        Notice notice = Notice.restore(
                1L, 10L, 1L,
                "3주차 과제 제출 안내",
                "이번 주 일요일까지 제출해주세요.",
                true, "COURSE", NoticeStatus.PUBLISHED,
                now, now
        );

        // then
        assertThat(notice.getId()).isEqualTo(1L);
        assertThat(notice.getAuthorId()).isEqualTo(10L);
        assertThat(notice.getCourseId()).isEqualTo(1L);
        assertThat(notice.getTitle()).isEqualTo("3주차 과제 제출 안내");
        assertThat(notice.getContent()).isEqualTo("이번 주 일요일까지 제출해주세요.");
        assertThat(notice.isPinned()).isTrue();
        assertThat(notice.getType()).isEqualTo("COURSE");
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.PUBLISHED);
    }
}