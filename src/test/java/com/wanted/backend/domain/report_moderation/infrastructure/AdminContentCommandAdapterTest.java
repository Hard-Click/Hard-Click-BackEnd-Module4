package com.wanted.backend.domain.report_moderation.infrastructure;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.report_moderation.infrastructure.persistence.AdminContentCommandAdapter;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminContentCommandAdapterTest {

    @InjectMocks
    private AdminContentCommandAdapter adminContentCommandAdapter;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("게시글 삭제 시 PostRepository.adminDeleteById가 호출된다")
    void deleteByAdmin_post() {
        // when
        adminContentCommandAdapter.deleteByAdmin(TargetType.POST, 15L);

        // then
        verify(postRepository).adminDeleteById(15L);
    }

    @Test
    @DisplayName("댓글 삭제 시 CommentRepository.softDeleteByAdmin이 호출된다")
    void deleteByAdmin_comment() {
        // when
        adminContentCommandAdapter.deleteByAdmin(TargetType.COMMENT, 20L);

        // then
        verify(commentRepository).softDeleteByAdmin(eq(20L), any());
    }

    @Test
    @DisplayName("게시글이 존재하지 않으면 저장소의 예외가 그대로 전파된다")
    void deleteByAdmin_post_notFound() {
        // given
        willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND))
                .given(postRepository).adminDeleteById(15L);

        // when & then
        assertThatThrownBy(() -> adminContentCommandAdapter.deleteByAdmin(TargetType.POST, 15L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글이 존재하지 않으면 저장소의 예외가 그대로 전파된다")
    void deleteByAdmin_comment_notFound() {
        // given
        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
                .given(commentRepository).softDeleteByAdmin(eq(20L), any());

        // when & then
        assertThatThrownBy(() -> adminContentCommandAdapter.deleteByAdmin(TargetType.COMMENT, 20L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("리뷰 삭제 시 ReviewRepository.adminDeleteById가 호출된다")
    void deleteByAdmin_review() {
        // when
        adminContentCommandAdapter.deleteByAdmin(TargetType.REVIEW, 30L);

        // then
        verify(reviewRepository).adminDeleteById(30L);
    }

    @Test
    @DisplayName("리뷰가 존재하지 않으면 저장소의 예외가 그대로 전파된다")
    void deleteByAdmin_review_notFound() {
        // given
        willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND))
                .given(reviewRepository).adminDeleteById(30L);

        // when & then
        assertThatThrownBy(() -> adminContentCommandAdapter.deleteByAdmin(TargetType.REVIEW, 30L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.REVIEW_NOT_FOUND.getMessage());
    }
}
