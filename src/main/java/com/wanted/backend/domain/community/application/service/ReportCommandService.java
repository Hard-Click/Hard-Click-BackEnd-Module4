package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.CreateReportCommand;
import com.wanted.backend.domain.community.application.usecase.ReportCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Report;
import com.wanted.backend.domain.community.domain.model.TargetType;
import com.wanted.backend.domain.community.domain.repository.CommentRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.domain.repository.ReportRepository;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ReportCommandService implements ReportCommandUseCase {

    private static final int REPORT_FLAG_THRESHOLD = 5;

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    public ReportCommandService(ReportRepository reportRepository,
                                PostRepository postRepository,
                                CommentRepository commentRepository,
                                ReviewRepository reviewRepository) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Long create(CreateReportCommand command) {

        // 1. 중복 신고 체크
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                command.reporterId(), command.targetType(), command.targetId())) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 2. 대상 존재 여부 체크
        validateTargetExists(command.targetType(), command.targetId());

        // 3. 신고 저장
        Long reportId = reportRepository.save(Report.create(
                command.reporterId(),
                command.targetType(),
                command.targetId(),
                command.reportTypes(),
                command.reason()
        ));

        // 4. 누적 신고 5건 이상 시 로그 (추후 관리자 플래그 연동)
        int count = reportRepository.countByTargetTypeAndTargetId(
                command.targetType(), command.targetId());
        if (count >= REPORT_FLAG_THRESHOLD) {
            log.warn("[Report Flag] targetType: {}, targetId: {}, count: {}",
                    command.targetType(), command.targetId(), count);
        }

        return reportId;
    }

    private void validateTargetExists(TargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case POST -> postRepository.findById(targetId).isPresent();
            case COMMENT -> commentRepository.findById(targetId).isPresent();
            case REVIEW -> reviewRepository.findById(targetId).isPresent();
        };
        if (!exists) {
            throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }
}