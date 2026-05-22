package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final LessonRepository lessonRepository;

    /**
     * 영상 파일 비동기 처리
     * PENDING → PROCESSING → COMPLETED / FAILED
     * 호출 즉시 별도 스레드(fileProcessingExecutor)에서 실행되며, 호출부는 기다리지 않음
     */
    @Async("fileProcessingExecutor")
    public void process(Long lessonId) {
        log.info("[FileProcessing] 처리 시작 - lessonId={}", lessonId);

        // PROCESSING 상태로 전이
        updateStatus(lessonId, "PROCESSING");

        try {
            doProcess(lessonId);

            // COMPLETED 상태로 전이
            updateStatus(lessonId, "COMPLETED");
            log.info("[FileProcessing] 처리 완료 - lessonId={}", lessonId);

        } catch (Exception e) {
            // FAILED 상태로 전이
            log.error("[FileProcessing] 처리 실패 - lessonId={}, error={}", lessonId, e.getMessage());
            updateStatus(lessonId, "FAILED");
        }
    }

    /**
     * 실제 파일 처리 로직
     * 현재는 시뮬레이션 (추후 트랜스코딩, 썸네일 추출 등으로 교체)
     */
    private void doProcess(Long lessonId) throws InterruptedException {
        Thread.sleep(2000); // 실제 처리 시뮬레이션 (FFmpeg, AWS MediaConvert 등)
    }

    /**
     * 상태 전이 - 각 호출이 독립 트랜잭션으로 커밋되어 즉시 반영됨
     */
    private void updateStatus(Long lessonId, String status) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        switch (status) {
            case "PROCESSING" -> lesson.startProcessing();
            case "COMPLETED"  -> lesson.completeProcessing();
            case "FAILED"     -> lesson.failProcessing();
        }
        lessonRepository.save(lesson);
    }
}
