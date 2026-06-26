package com.wanted.backend.domain.community.infrastructure.member;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberNameAdapterTest {

    private final SpringDataMemberReferenceRepository memberReferenceRepository = mock(SpringDataMemberReferenceRepository.class);
    private final MemberNameAdapter adapter = new MemberNameAdapter(memberReferenceRepository);

    @Test
    void returnsNamesKeyedByMemberId() {
        MemberReferenceEntity first = memberReference(1L, "김지훈");
        MemberReferenceEntity second = memberReference(2L, "이서연");
        when(memberReferenceRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(first, second));

        Map<Long, String> result = adapter.getNamesByMemberIds(List.of(1L, 2L));

        assertThat(result).containsEntry(1L, "김지훈").containsEntry(2L, "이서연");
    }

    @Test
    void returnsEmptyMapWhenNoMembersFound() {
        when(memberReferenceRepository.findAllById(List.of(999L))).thenReturn(List.of());

        Map<Long, String> result = adapter.getNamesByMemberIds(List.of(999L));

        assertThat(result).isEmpty();
    }

    @Test
    void doesNotThrowWhenNameIsNull() {
        MemberReferenceEntity withNullName = memberReference(1L, null);
        when(memberReferenceRepository.findAllById(List.of(1L))).thenReturn(List.of(withNullName));

        Map<Long, String> result = adapter.getNamesByMemberIds(List.of(1L));

        assertThat(result).containsEntry(1L, null);
    }

    private MemberReferenceEntity memberReference(Long id, String name) {
        MemberReferenceEntity entity = mock(MemberReferenceEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getName()).thenReturn(name);
        return entity;
    }
}
