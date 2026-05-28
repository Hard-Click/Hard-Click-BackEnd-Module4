package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.usecase.CheckDuplicateUseCase;
import com.wanted.backend.domain.identity.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckDuplicateService implements CheckDuplicateUseCase {

    private final MemberRepository memberRepository;

    @Override
    public boolean isUsernameDuplicated(String username) {
        return memberRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }
}