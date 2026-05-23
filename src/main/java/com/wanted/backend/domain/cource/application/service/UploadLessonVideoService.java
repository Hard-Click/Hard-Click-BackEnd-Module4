package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.domain.cource.application.usecase.UploadLessonVideoUseCase;
import com.wanted.backend.domain.cource.domain.model.Lesson;
import com.wanted.backend.domain.cource.domain.repository.LessonRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class UploadLessonVideoService implements UploadLessonVideoUseCase {

    private final LessonRepository lessonRepository;
    private final VideoStoragePort videoStoragePort;
    private final FileProcessingService fileProcessingService;

    @Transactional
    @Override
    public String handle(UploadLessonVideoCommand command) {
        Lesson lesson = lessonRepository.findById(command.lessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        // Port라는 연결 통로를 통해서 실제 저장소(예: AWS S3)에 동영상 파일을 진짜로 집어넣고,
        // 저장된 인터넷 주소(URL)를 받아오는 실제 실행 핵심 기능(메서드)
        String videoUrl = videoStoragePort.store(
                command.lessonId(),
                command.originalFilename(),
                command.videoData()
        );

        // 영상 URL 저장 + 상태를 PENDING으로 전이
        lesson.attachVideo(videoUrl);
        lessonRepository.save(lesson);

        // 트랜잭션 커밋 후에 비동기 처리 시작 — 커밋 전에 시작하면 async 스레드가
        // 아직 커밋되지 않은 행을 읽어 videoUrl이 null인 채로 처리될 수 있음
        Long lessonId = command.lessonId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileProcessingService.process(lessonId);
            }
        });

        return videoUrl;
    }
}
