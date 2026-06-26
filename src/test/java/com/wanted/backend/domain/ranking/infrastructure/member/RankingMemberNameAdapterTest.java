package com.wanted.backend.domain.ranking.infrastructure.member;

import com.wanted.backend.domain.community.application.port.MemberNamePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingMemberNameAdapterTest {

    private final MemberNamePort communityMemberNamePort = mock(MemberNamePort.class);
    private final RankingMemberNameAdapter adapter = new RankingMemberNameAdapter(communityMemberNamePort);

    @Test
    void delegatesToCommunityMemberNamePort() {
        when(communityMemberNamePort.getNamesByMemberIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, "김지훈", 2L, "이서연"));

        Map<Long, String> result = adapter.getNamesByMemberIds(List.of(1L, 2L));

        assertThat(result).containsEntry(1L, "김지훈").containsEntry(2L, "이서연");
    }
}
