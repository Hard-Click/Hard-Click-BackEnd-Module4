package com.wanted.backend.domain.cart.infrastructure.course;

import com.wanted.backend.domain.cart.application.port.CartCourseQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartCourseQueryAdapter implements CartCourseQueryPort {

    private final CartCourseJpaRepository courseRepository;
    private final CartMemberJpaRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDetail> findAllByIds(List<Long> courseIds) {
        List<CartCourseJpaEntity> courses = courseRepository.findAllByIdIn(courseIds);

        List<Long> authorIds = courses.stream().map(CartCourseJpaEntity::getAuthorId).distinct().toList();
        Map<Long, String> memberNames = memberRepository.findByIdIn(authorIds).stream()
                .collect(Collectors.toMap(CartMemberJpaEntity::getId, CartMemberJpaEntity::getName));

        return courses.stream()
                .map(c -> new CourseDetail(
                        c.getId(),
                        c.getTitle(),
                        c.getPrice(),
                        memberNames.getOrDefault(c.getAuthorId(), "")
                ))
                .toList();
    }
}
