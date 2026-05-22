package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import com.wanted.backend.domain.identity.domain.model.Member;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MemberPersistenceAdapter implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberPersistenceAdapter(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return memberJpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity;

        if (member.getId() == null) {
            // 1. 신규 생성: 도메인 객체를 기반으로 새로운 JPA 엔티티 생성
            entity = new MemberJpaEntity(
                    member.getUsername(), member.getEmail(), member.getPassword(),
                    member.getName(), member.getGender(), member.getBirthDate(),
                    member.getPhoneNumber(), member.getProfileImageUrl(), member.getRole(),
                    member.getStatus(), member.isPasswordChangeRequired(), member.getLoginFailCount(),
                    member.isLocked(), member.getLockedAt(), member.getLastLoginAt(),
                    member.getCreatedAt(), member.getUpdatedAt()
            );
        } else {
            // 2. 업데이트: 영속성 컨텍스트에서 기존 엔티티를 찾아 상태를 동기화
            // (주의: 실무에서는 성능을 위해 JpaRepository.findById() 등으로 영속 상태 엔티티를 먼저 가져오는 방식을 쓰기도 합니다.
            // 여기서는 JpaRepository.save()가 병합(merge)을 처리하도록 위임하되, 상태를 맞춰줍니다.)
            entity = memberJpaRepository.findById(member.getId())
                    .orElseThrow(() -> new IllegalStateException("Member entity not found for update: " + member.getId()));

            // 영속 상태의 엔티티에 도메인의 변경된 상태를 부어넣음 (이후 트랜잭션 종료 시 Dirty Checking으로 자동 Update 발생)
            entity.updateFromDomain(member);
        }

        MemberJpaEntity savedEntity = memberJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return memberJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    private Member toDomain(MemberJpaEntity entity) {
        return Member.restore(
                entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPassword(),
                entity.getName(), entity.getGender(), entity.getBirthDate(), entity.getPhoneNumber(),
                entity.getProfileImageUrl(), entity.getRole(), entity.getStatus(),
                entity.isPasswordChangeRequired(), entity.getLoginFailCount(), entity.isLocked(),
                entity.getLockedAt(), entity.getLastLoginAt(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}