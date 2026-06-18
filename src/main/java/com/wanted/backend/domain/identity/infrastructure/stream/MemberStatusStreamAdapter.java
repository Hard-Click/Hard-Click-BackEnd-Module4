package com.wanted.backend.domain.identity.infrastructure.stream;

import com.wanted.backend.domain.identity.application.dto.MemberStatusChangedMessage;
import com.wanted.backend.domain.identity.application.dto.MemberStatusSyncMessage;
import com.wanted.backend.domain.identity.application.port.MemberStatusStreamPort;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MemberStatusStreamAdapter implements MemberStatusStreamPort {

    private static final long TIMEOUT_MILLIS = 30L * 60L * 1000L;
    private static final String STATUS_CHANGED_EVENT_NAME = "MEMBER_STATUS_CHANGED";
    private static final String STATUS_SYNC_EVENT_NAME = "MEMBER_STATUS_SYNC";
    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";
    private static final String HEARTBEAT_DATA = "ping";

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long memberId, MemberStatusSyncMessage syncMessage) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emitters.computeIfAbsent(memberId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(() -> remove(memberId, emitter));
        emitter.onError(error -> remove(memberId, emitter));
        sendToEmitter(memberId, emitter, STATUS_SYNC_EVENT_NAME, syncMessage);

        return emitter;
    }

    @Override
    public void send(MemberStatusChangedMessage message) {
        List<SseEmitter> memberEmitters = emitters.get(message.memberId());
        if (memberEmitters == null || memberEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : memberEmitters) {
            sendToEmitter(message.memberId(), emitter, STATUS_CHANGED_EVENT_NAME, message);
        }
    }

    @Override
    public void sendHeartbeat() {
        for (Map.Entry<Long, List<SseEmitter>> entry : emitters.entrySet()) {
            Long memberId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                sendToEmitter(memberId, emitter, HEARTBEAT_EVENT_NAME, HEARTBEAT_DATA);
            }
        }
    }

    private void sendToEmitter(Long memberId, SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            remove(memberId, emitter);
        }
    }

    private void remove(Long memberId, SseEmitter emitter) {
        emitters.computeIfPresent(memberId, (id, memberEmitters) -> {
            memberEmitters.remove(emitter);
            return memberEmitters.isEmpty() ? null : memberEmitters;
        });
    }
}
