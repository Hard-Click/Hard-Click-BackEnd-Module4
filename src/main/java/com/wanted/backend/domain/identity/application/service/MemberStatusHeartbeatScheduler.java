package com.wanted.backend.domain.identity.application.service;

import com.wanted.backend.domain.identity.application.port.MemberStatusStreamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberStatusHeartbeatScheduler {

    private final MemberStatusStreamPort memberStatusStreamPort;

    @Scheduled(fixedRateString = "${identity.member-status-stream.heartbeat-rate-ms:30000}")
    public void sendHeartbeat() {
        memberStatusStreamPort.sendHeartbeat();
    }
}
