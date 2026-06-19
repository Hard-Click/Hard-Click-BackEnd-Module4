package com.wanted.backend.domain.study_timer.infrastructure.lock;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.jpa.SpecHints.HINT_SPEC_QUERY_TIMEOUT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberRowLockAdapterTest {

    private static final int LOCK_TIMEOUT_MILLISECONDS = 3_000;

    private EntityManager entityManager;
    private Query query;
    private MemberRowLockAdapter adapter;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        query = mock(Query.class);
        StudyTimerMemberLockProperties properties = new StudyTimerMemberLockProperties();
        properties.setTimeoutMilliseconds(LOCK_TIMEOUT_MILLISECONDS);
        adapter = new MemberRowLockAdapter(entityManager, properties);
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
        when(query.setHint(HINT_SPEC_QUERY_TIMEOUT, LOCK_TIMEOUT_MILLISECONDS)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(1L));

        adapter.lock(1L);

        verify(query).setParameter("memberId", 1L);
        verify(query).setHint(HINT_SPEC_QUERY_TIMEOUT, LOCK_TIMEOUT_MILLISECONDS);
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
        when(query.setHint(HINT_SPEC_QUERY_TIMEOUT, LOCK_TIMEOUT_MILLISECONDS)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertThatThrownBy(() -> adapter.lock(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void throwsLockTimeoutWhenRowLockWaitTimeoutOccurs() {
        when(entityManager.createNativeQuery("""
                        select member_id
                        from members
                        where member_id = :memberId
                        for update
                        """))
                .thenReturn(query);
        when(query.setParameter("memberId", 1L)).thenReturn(query);
        when(query.setHint(HINT_SPEC_QUERY_TIMEOUT, LOCK_TIMEOUT_MILLISECONDS)).thenReturn(query);
        when(query.getResultList()).thenThrow(new QueryTimeoutException("lock timeout"));

        assertThatThrownBy(() -> adapter.lock(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDY_TIMER_LOCK_TIMEOUT);
    }
}
