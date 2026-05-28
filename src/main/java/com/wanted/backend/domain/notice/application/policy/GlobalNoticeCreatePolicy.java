package com.wanted.backend.domain.notice.application.policy;

import com.wanted.backend.domain.notice.application.port.AdminValidationPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;


@Component
public class GlobalNoticeCreatePolicy {

    private final AdminValidationPort adminValidationPort;

    public GlobalNoticeCreatePolicy(AdminValidationPort adminValidationPort) {
        this.adminValidationPort = adminValidationPort;
    }

    public void validate(Long memberId) {

        // [1단계] 관리자 여부 확인
        if (!adminValidationPort.isAdmin(memberId)) {
            throw new BusinessException(ErrorCode.NOTICE_ADMIN_ONLY);
        }
    }
}