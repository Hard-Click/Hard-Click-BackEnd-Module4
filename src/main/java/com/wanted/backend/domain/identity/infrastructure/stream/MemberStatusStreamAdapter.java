package com.wanted.backend.domain.identity.infrastructure.stream;

import com.wanted.backend.domain.identity.application.dto.MemberStatusChangedMessage;
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
    private static final String EVENT_NAME = "MEMBER_STATUS_CHANGED";

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emitters.computeIfAbsent(memberId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(() -> remove(memberId, emitter));
        emitter.onError(error -> remove(memberId, emitter));

        return emitter;
    }

    @Override
    public void send(MemberStatusChangedMessage message) {
        List<SseEmitter> memberEmitters = emitters.get(message.memberId());
        if (memberEmitters == null || memberEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : memberEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENT_NAME)
                        .data(message));
            } catch (IOException | IllegalStateException e) {
                remove(message.memberId(), emitter);
            }
        }
    }

    private void remove(Long memberId, SseEmitter emitter) {
        List<SseEmitter> memberEmitters = emitters.get(memberId);
        if (memberEmitters == null) {
            return;
        }

        memberEmitters.remove(emitter);
        if (memberEmitters.isEmpty()) {
            emitters.remove(memberId);
        }
    }
}
