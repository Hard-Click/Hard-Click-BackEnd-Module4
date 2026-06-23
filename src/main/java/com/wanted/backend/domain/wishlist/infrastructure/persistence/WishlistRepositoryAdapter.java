package com.wanted.backend.domain.wishlist.infrastructure.persistence;

import com.wanted.backend.domain.wishlist.domain.model.WishlistItem;
import com.wanted.backend.domain.wishlist.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WishlistRepositoryAdapter implements WishlistRepository {

    private final WishlistItemJpaRepository repository;

    @Override
    @Transactional
    public WishlistItem save(WishlistItem item) {
        WishlistItemJpaEntity entity = WishlistItemJpaEntity.create(item.getMemberId(), item.getCourseId());
        WishlistItemJpaEntity saved = repository.save(entity);
        return WishlistItem.restore(saved.getId(), saved.getMemberId(), saved.getCourseId(), saved.getAddedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItem> findAllByMemberId(Long memberId) {
        return repository.findByMemberId(memberId).stream()
                .map(e -> WishlistItem.restore(e.getId(), e.getMemberId(), e.getCourseId(), e.getAddedAt()))
                .toList();
    }

    @Override
    @Transactional
    public void deleteByMemberIdAndCourseId(Long memberId, Long courseId) {
        repository.deleteByMemberIdAndCourseId(memberId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMemberIdAndCourseId(Long memberId, Long courseId) {
        return repository.existsByMemberIdAndCourseId(memberId, courseId);
    }
}
