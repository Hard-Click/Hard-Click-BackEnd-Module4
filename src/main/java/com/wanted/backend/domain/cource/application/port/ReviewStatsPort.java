package com.wanted.backend.domain.cource.application.port;

import java.util.Collection;
import java.util.Map;

public interface ReviewStatsPort {
    double avgRating(Long courseId);
    int reviewCount(Long courseId);

    /**
     * 강의 목록처럼 여러 강의의 통계가 한 번에 필요할 때 N+1 없이 일괄 조회.
     * 결과에 없는 courseId는 리뷰가 0건인 강의로 간주한다.
     */
    Map<Long, Stats> findStatsByCourseIds(Collection<Long> courseIds);

    record Stats(double avgRating, int reviewCount) {
        public static final Stats EMPTY = new Stats(0.0, 0);
    }
}
