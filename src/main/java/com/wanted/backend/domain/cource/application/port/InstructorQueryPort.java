package com.wanted.backend.domain.cource.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 강사(회원) 정보 조회 Port
 * 크로스 컨텍스트 직접 참조 금지 → 이 포트를 통해서만 identity 도메인 데이터 접근
 */
public interface InstructorQueryPort {
    /** authorId 목록 → id:이름 맵 */
    Map<Long, String> findNamesByIds(Collection<Long> ids);

    /** 강사명 검색 → authorId 목록 */
    List<Long> findIdsByName(String name);

    /** 강사 ID → 한줄소개/자기소개/경력 */
    InstructorProfile findProfileById(Long instructorId);

    record InstructorProfile(String oneLineIntro, String introduction, String career) {
        public static InstructorProfile empty() {
            return new InstructorProfile(null, null, null);
        }
    }
}
