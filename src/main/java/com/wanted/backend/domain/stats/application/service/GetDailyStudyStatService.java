package com.wanted.backend.domain.stats.application.service;

import com.wanted.backend.domain.stats.application.query.GetDailyStudyStatQuery;
import com.wanted.backend.domain.stats.application.usecase.GetDailyStudyStatUseCase;
import com.wanted.backend.domain.stats.domain.model.DailyStudyStat;
import com.wanted.backend.domain.stats.domain.repository.DailyStudyStatsRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDailyStudyStatService implements GetDailyStudyStatUseCase {

    private final DailyStudyStatsRepository dailyStudyStatsRepository;

    @Override
    public DailyStudyStatView handle(GetDailyStudyStatQuery query) {
        validate(query);

        DailyStudyStat stat = dailyStudyStatsRepository
                .findByMemberIdAndStatDate(query.memberId(), query.date())
                .orElseGet(() -> DailyStudyStat.empty(query.memberId(), query.date()));

        return new DailyStudyStatView(
                stat.statDate(),
                stat.watchedLessonCount(),
                stat.studySeconds(),
                stat.completedLessonCount()
        );
    }

    private void validate(GetDailyStudyStatQuery query) {
        if (query.memberId() == null) {
            throw new BusinessException(ErrorCode.DAILY_STATS_MEMBER_ID_REQUIRED);
        }
        if (query.date() == null) {
            throw new BusinessException(ErrorCode.DAILY_STATS_DATE_REQUIRED);
        }
    }
}
