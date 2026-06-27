package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.infrastructure.member.PaymentMemberReferenceEntity;
import com.wanted.backend.domain.payment.infrastructure.member.PaymentMemberReferenceRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 관리자 결제 목록 조회. 실제 결제 확정은 orders 테이블에 기록되므로(payment 테이블 아님)
 * orders 기반으로 조회한다. AdminPaymentData.paymentId 자리에는 orderId를 담아
 * 환불(관리자) 시 orderId로 사용한다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentQueryAdapter implements AdminPaymentQueryPort {

    // 결제 이력으로 노출할 주문 상태(미결제 READY 제외)
    private static final Set<String> VISIBLE_STATUSES = Set.of(
            "PAID", "PARTIAL_REFUNDED", "REFUNDED", "CANCELED"
    );

    private final OrderJpaRepository orderRepository;
    private final PaymentMemberReferenceRepository memberRepository;

    @Override
    public Page<AdminPaymentData> search(PaymentStatus status, String keyword, Pageable pageable) {
        Set<String> statusFilter = toOrderStatuses(status);
        if (statusFilter.isEmpty()) {
            return Page.empty(pageable);
        }

        Specification<OrderJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(statusFilter));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";

                Subquery<Long> memberSub = query.subquery(Long.class);
                var memberRoot = memberSub.from(PaymentMemberReferenceEntity.class);
                memberSub.select(memberRoot.get("id"))
                        .where(cb.or(
                                cb.like(cb.lower(memberRoot.get("name")), like),
                                cb.like(cb.lower(memberRoot.get("email")), like)
                        ));

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderNo")), like),
                        root.get("memberId").in(memberSub)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OrderJpaEntity> orders = orderRepository.findAll(spec, pageable);
        if (orders.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> memberIds = orders.getContent().stream()
                .map(OrderJpaEntity::getMemberId)
                .distinct()
                .toList();
        Map<Long, PaymentMemberReferenceEntity> memberById = memberRepository.findByIdIn(memberIds).stream()
                .collect(Collectors.toMap(PaymentMemberReferenceEntity::getId, Function.identity()));

        List<AdminPaymentData> content = orders.getContent().stream()
                .map(order -> toData(order, memberById.get(order.getMemberId())))
                .toList();

        return new PageImpl<>(content, pageable, orders.getTotalElements());
    }

    private AdminPaymentData toData(OrderJpaEntity order, PaymentMemberReferenceEntity member) {
        return new AdminPaymentData(
                order.getId(),                       // paymentId 자리 = orderId (환불 시 사용)
                order.getId(),
                order.getOrderNo(),
                order.getPaymentType(),
                member == null ? null : member.getName(),
                member == null ? null : member.getEmail(),
                order.getFinalAmount(),
                toPaymentStatus(order.getStatus()),
                order.getPaidAt()
        );
    }

    // 결제 상태 필터(PaymentStatus) → orders.status 집합 역매핑
    private Set<String> toOrderStatuses(PaymentStatus status) {
        if (status == null) {
            return VISIBLE_STATUSES;
        }
        return switch (status) {
            case PAID -> Set.of("PAID", "PARTIAL_REFUNDED");
            case REFUNDED -> Set.of("REFUNDED");
            case CANCELED -> Set.of("CANCELED");
            // PENDING/READY/FAILED 는 orders 기준 노출 대상이 없음
            default -> Set.of();
        };
    }

    private PaymentStatus toPaymentStatus(String orderStatus) {
        return switch (orderStatus) {
            case "PAID", "PARTIAL_REFUNDED" -> PaymentStatus.PAID;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            case "CANCELED" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.READY;
        };
    }
}
