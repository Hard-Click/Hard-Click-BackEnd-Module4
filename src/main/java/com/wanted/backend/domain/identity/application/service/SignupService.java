package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.presentation.api.request.SignupStepOneRequest;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    public void signup(SignupStepOneRequest request) {


        // if (memberRepository.existsByUsername(request.getUsername())) {
        //     throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        // }


        // if (memberRepository.existsByEmail(request.getEmail())) {
        //     throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        // }

        if (!request.getPassword()
                .equals(request.getPasswordConfirm())) {

            throw new BusinessException(
                    ErrorCode.PASSWORD_MISMATCH
            );
        }
    }
}