package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.policy.CommunityAccessPolicy;
import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.domain.community.application.port.MemberNamePort;
import com.wanted.backend.domain.community.application.result.CommentListResult;
import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @InjectMocks
    private CommentQueryService commentQueryService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberNamePort memberNamePort;

    @Mock
    private CommunityFileStoragePort fileStoragePort;

    @Mock
    private CommunityAccessPolicy communityAccessPolicy;

    @Test
    @DisplayName("작성자 이름이 빈 문자열이어도 예외 없이 조회된다")
    void getComments_authorNameIsEmptyString_doesNotThrow() {
        // given
        Long postId = 31L;
        Comment comment = Comment.restore(74L, postId, 1L, null,
                "내용", false, false, null, LocalDateTime.now(), LocalDateTime.now());

        given(commentRepository.findByPostIdAndParentIdIsNull(postId)).willReturn(List.of(comment));
        given(commentRepository.findByParentIdIn(List.of(74L))).willReturn(List.of());
        given(memberNamePort.getNamesByMemberIds(Set.of(1L))).willReturn(Map.of(1L, ""));
        given(commentRepository.countByPostId(postId)).willReturn(1);

        // when
        CommentListResult result = commentQueryService.getComments(postId, null, false);

        // then
        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().get(0).authorName()).isEmpty();
        assertThat(result.comments().get(0).authorInitial()).isEmpty();
    }
}
