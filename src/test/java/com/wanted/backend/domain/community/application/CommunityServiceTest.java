package com.wanted.backend.domain.community.application;

import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.policy.CommentAcceptPolicy;
import com.wanted.backend.domain.community.application.service.CommentCommandService;
import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @InjectMocks
    private CommentCommandService commentCommandService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentAcceptPolicy commentAcceptPolicy;

    @Test
    @DisplayName("대댓글에 답글을 달면 예외가 발생하고 저장이 호출되지 않는다")
    void createComment_fail_replyDepthExceeded() {
        // given
        CreateCommentCommand command = new CreateCommentCommand(1L, 1L, 2L, "내용", null);

        Comment parentComment = Comment.restore(2L, 1L, 1L, 1L,
                "부모 댓글", false, false, null, LocalDateTime.now(), LocalDateTime.now());

        given(postRepository.findById(1L)).willReturn(Optional.of(
                Post.restore(1L, 1L, BoardType.FREE, null, "제목", "내용",
                        0, false, LocalDateTime.now(), LocalDateTime.now())));
        given(commentRepository.findById(2L)).willReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentCommandService.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REPLY_DEPTH_EXCEEDED.getMessage());

        verify(commentRepository, never()).save(any());
    }
}