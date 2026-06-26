package com.wanted.backend.domain.order.infrastructure.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderCourseRefRepository extends JpaRepository<OrderCourseRefEntity, Long> {

    List<OrderCourseRefEntity> findByIdIn(Collection<Long> ids);
}
