package com.wanted.backend.domain.community.domain;

import com.wanted.backend.domain.community.domain.model.Comment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityDomainTest {

    @Test
    @DisplayName("대댓글이 있는 댓글을 삭제하면 내용이 '삭제된 댓글입니다.'로 변경되고 isDeleted가 true가 된다")
    void softDelete_contentChangedAndDeletedTrue() {
        // given
        Comment comment = Comment.restore(1L, 1L, 1L, null,
                "원본 댓글 내용", false, false, null,
                LocalDateTime.now(), LocalDateTime.now());

        // when
        comment.softDelete();

        // then
        assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다.");
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getImageUrl()).isNull();
    }
}