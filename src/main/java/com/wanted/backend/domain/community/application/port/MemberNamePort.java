package com.wanted.backend.domain.community.application.port;

/*
 * [Application Layer - Port Interface]
 * community BC가 identity BC의 회원 이름을 가져오기 위한 계약이다.
 *
 * 왜 Port가 필요한가?
 * - reviews는 community BC, members는 identity BC 소속이다.
 * - 다른 BC의 데이터를 직접 참조하면 BC 간 강한 결합이 생긴다.
 * - Port로 추상화하면 identity BC 구현이 바뀌어도 community BC는 영향받지 않는다.
 *
 * 흐름:
 * ReviewQueryService → MemberNamePort (interface)
 *                          → MemberNameAdapter (infrastructure 구현체)
 *                              → SpringDataMemberReferenceRepository
 *                                  → members 테이블 (읽기 전용)
 */
public interface MemberNamePort {
    String getNameByMemberId(Long memberId);
}