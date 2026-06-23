package com.wanted.backend.domain.cart.infrastructure.persistence;

import com.wanted.backend.domain.cart.domain.model.CartItem;
import com.wanted.backend.domain.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartItemJpaRepository repository;

    @Override
    @Transactional
    public CartItem save(CartItem cartItem) {
        CartItemJpaEntity entity = CartItemJpaEntity.create(cartItem.getMemberId(), cartItem.getCourseId());
        CartItemJpaEntity saved = repository.save(entity);
        return CartItem.restore(saved.getId(), saved.getMemberId(), saved.getCourseId(), saved.getAddedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> findAllByMemberId(Long memberId) {
        return repository.findByMemberId(memberId).stream()
                .map(e -> CartItem.restore(e.getId(), e.getMemberId(), e.getCourseId(), e.getAddedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CartItem> findByMemberIdAndCourseId(Long memberId, Long courseId) {
        return repository.findByMemberIdAndCourseId(memberId, courseId)
                .map(e -> CartItem.restore(e.getId(), e.getMemberId(), e.getCourseId(), e.getAddedAt()));
    }

    @Override
    @Transactional
    public void deleteByMemberIdAndCourseId(Long memberId, Long courseId) {
        repository.deleteByMemberIdAndCourseId(memberId, courseId);
    }

    @Override
    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        repository.deleteAllByMemberId(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMemberIdAndCourseId(Long memberId, Long courseId) {
        return repository.existsByMemberIdAndCourseId(memberId, courseId);
    }
}
