package com.wanted.backend.domain.cource.application.port;

import java.util.Collection;
import java.util.Map;

public interface EnrollmentStatsPort {
    int enrollmentCount(Long courseId);

    /**
     * 강의 목록처럼 여러 강의의 수강생 수가 한 번에 필요할 때 N+1 없이 일괄 조회.
     * 결과에 없는 courseId는 수강생이 0명인 강의로 간주한다.
     */
    Map<Long, Integer> countByCourseIds(Collection<Long> courseIds);
}
