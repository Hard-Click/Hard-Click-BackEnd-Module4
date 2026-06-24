package com.wanted.backend.domain.order.infrastructure.enrollment;

import com.wanted.backend.domain.order.application.port.OrderEnrollmentRevocationPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * enrollment 도메인의 Enrollment 엔티티를 직접 참조하지 않고
 * native SQL로 enrollment 테이블만 갱신 (크로스 컨텍스트 규칙 준수)
 */
@Component
public class OrderEnrollmentRevocationAdapter implements OrderEnrollmentRevocationPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void revoke(Long memberId, Long courseId) {
        int updated = em.createNativeQuery(
                        "UPDATE enrollment SET status = 'REFUNDED' " +
                                "WHERE member_id = :memberId AND course_id = :courseId AND status <> 'REFUNDED'")
                .setParameter("memberId", memberId)
                .setParameter("courseId", courseId)
                .executeUpdate();
        if (updated != 1) {
            throw new BusinessException(ErrorCode.ORDER_ENROLLMENT_REVOKE_FAILED);
        }
    }
}
