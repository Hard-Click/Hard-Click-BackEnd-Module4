package com.wanted.backend.domain.study_timer.infrastructure.lock;

import com.wanted.backend.domain.study_timer.application.port.MemberLockPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberRowLockAdapter implements MemberLockPort {

    private final EntityManager entityManager;

    @Override
    public void lock(Long memberId) {
        List<?> rows = entityManager.createNativeQuery("""
                        select member_id
                        from members
                        where member_id = :memberId
                        for update
                        """)
                .setParameter("memberId", memberId)
                .getResultList();

        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
