package com.wanted.backend.domain.study_timer.infrastructure.lock;

import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.Query;
import jakarta.persistence.QueryTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hibernate.jpa.SpecHints.HINT_SPEC_QUERY_TIMEOUT;

@Component
@RequiredArgsConstructor
public class MemberRowLockAdapter implements MemberLockPort {

    private final EntityManager entityManager;
    private final StudyTimerMemberLockProperties memberLockProperties;

    @Override
    public void lock(Long memberId) {
        Query query = entityManager.createNativeQuery("""
                select member_id
                from members
                where member_id = :memberId
                for update
                """)
                .setParameter("memberId", memberId)
                .setHint(HINT_SPEC_QUERY_TIMEOUT, memberLockProperties.timeoutMilliseconds());

        List<?> rows;
        try {
            rows = query.getResultList();
        } catch (QueryTimeoutException | LockTimeoutException e) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_LOCK_TIMEOUT);
        }

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
