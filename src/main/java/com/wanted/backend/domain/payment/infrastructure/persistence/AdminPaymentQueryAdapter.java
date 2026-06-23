package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentQueryAdapter implements AdminPaymentQueryPort {

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final PaymentMemberReferenceRepository memberRepository;

    @Override
    public Page<AdminPaymentData> search(PaymentStatus status, String keyword, Pageable pageable) {
        Specification<PaymentJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status.name()));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";

                Subquery<Long> orderSub = query.subquery(Long.class);
                var orderRoot = orderSub.from(OrderJpaEntity.class);
                orderSub.select(orderRoot.get("id"))
                        .where(cb.like(cb.lower(orderRoot.get("orderNo")), like));

                Subquery<Long> memberSub = query.subquery(Long.class);
                var memberRoot = memberSub.from(PaymentMemberReferenceEntity.class);
                memberSub.select(memberRoot.get("id"))
                        .where(cb.or(
                                cb.like(cb.lower(memberRoot.get("name")), like),
                                cb.like(cb.lower(memberRoot.get("email")), like)
                        ));

                predicates.add(cb.or(
                        root.get("orderId").in(orderSub),
                        root.get("memberId").in(memberSub)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PaymentJpaEntity> payments = paymentRepository.findAll(spec, pageable);
        if (payments.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderIds = payments.getContent().stream()
                .map(PaymentJpaEntity::getOrderId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<Long> memberIds = payments.getContent().stream()
                .map(PaymentJpaEntity::getMemberId)
                .distinct()
                .toList();

        Map<Long, OrderJpaEntity> orderById = orderRepository.findByIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderJpaEntity::getId, Function.identity()));
        Map<Long, PaymentMemberReferenceEntity> memberById = memberRepository.findByIdIn(memberIds).stream()
                .collect(Collectors.toMap(PaymentMemberReferenceEntity::getId, Function.identity()));

        List<AdminPaymentData> content = payments.getContent().stream()
                .map(payment -> toData(payment, orderById.get(payment.getOrderId()), memberById.get(payment.getMemberId())))
                .toList();

        return new PageImpl<>(content, pageable, payments.getTotalElements());
    }

    private AdminPaymentData toData(PaymentJpaEntity payment, OrderJpaEntity order, PaymentMemberReferenceEntity member) {
        PaymentType paymentType = order == null ? null : order.getPaymentType();
        return new AdminPaymentData(
                payment.getId(),
                payment.getOrderId(),
                order == null ? null : order.getOrderNo(),
                paymentType,
                member == null ? null : member.getName(),
                member == null ? null : member.getEmail(),
                payment.getPaidAmount(),
                PaymentStatus.from(payment.getStatus()),
                payment.getPaidAt()
        );
    }
}
