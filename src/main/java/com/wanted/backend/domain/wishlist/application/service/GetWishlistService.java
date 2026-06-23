package com.wanted.backend.domain.wishlist.application.service;

import com.wanted.backend.domain.wishlist.application.port.WishlistCourseDetailQueryPort;
import com.wanted.backend.domain.wishlist.application.usecase.GetWishlistUseCase;
import com.wanted.backend.domain.wishlist.domain.model.WishlistItem;
import com.wanted.backend.domain.wishlist.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetWishlistService implements GetWishlistUseCase {

    private final WishlistRepository wishlistRepository;
    private final WishlistCourseDetailQueryPort wishlistCourseDetailQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Item> handle(Long memberId) {
        List<WishlistItem> items = wishlistRepository.findAllByMemberId(memberId);
        if (items.isEmpty()) return List.of();

        List<Long> courseIds = items.stream().map(WishlistItem::getCourseId).toList();
        List<WishlistCourseDetailQueryPort.CourseDetail> details =
                wishlistCourseDetailQueryPort.findAllByIds(courseIds, memberId);

        return details.stream()
                .map(d -> new Item(
                        d.courseId(), d.title(), d.subject(), d.thumbnailUrl(), d.priceType(),
                        d.instructorName(), d.price(),
                        d.averageRating(), d.reviewCount(), d.enrollmentCount(), d.enrolled(), d.inCart()
                ))
                .toList();
    }
}
