package com.wanted.backend.domain.study_timer.infrastructure.lock;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberRowLockAdapterTest {

    private EntityManager entityManager;
    private Query query;
    private MemberRowLockAdapter adapter;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        query = mock(Query.class);
        adapter = new MemberRowLockAdapter(entityManager);
    }

    @Test
    void acquiresMemberRowLockWithNativeQuery() {
        when(entityManager.createNativeQuery("""
                        select member_id
                        from members
                        where member_id = :memberId
                        for update
                        """))
                .thenReturn(query);
        when(query.setParameter("memberId", 1L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(1L));

        adapter.lock(1L);

        verify(query).setParameter("memberId", 1L);
        verify(query).getResultList();
    }

    @Test
    void throwsUserNotFoundWhenLockTargetDoesNotExist() {
        when(entityManager.createNativeQuery("""
                        select member_id
                        from members
                        where member_id = :memberId
                        for update
                        """))
                .thenReturn(query);
        when(query.setParameter("memberId", 1L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertThatThrownBy(() -> adapter.lock(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
