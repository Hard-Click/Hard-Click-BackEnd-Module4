package com.wanted.backend.domain.community.application;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.service.PostCommandService;
import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.repository.PostFileRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.notification.domain.repository.NotificationRepository;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceAccessTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private CommunityFileStoragePort storagePort;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostFileRepository postFileRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CommunityAccessPolicy communityAccessPolicy;

    @Test
    @DisplayName("정지/탈퇴 회원이 게시글을 작성하면 차단되고 저장이 호출되지 않는다")
    void createPost_fail_whenAccessDenied() {
        // given
        CreatePostCommand command = new CreatePostCommand(
                1L, BoardType.FREE, null, "제목", "내용", null);

        willThrow(new BusinessException(ErrorCode.COMMUNITY_ACCESS_DENIED))
                .given(communityAccessPolicy).validateAccess(1L);

        // when & then
        assertThatThrownBy(() -> postCommandService.create(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.COMMUNITY_ACCESS_DENIED.getMessage());

        verify(postRepository, never()).save(any());
    }
}
