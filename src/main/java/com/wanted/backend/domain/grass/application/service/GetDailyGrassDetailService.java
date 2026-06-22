package com.wanted.backend.domain.grass.application.service;

import com.wanted.backend.domain.grass.application.query.GetDailyGrassDetailQuery;
import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase;
import com.wanted.backend.domain.grass.domain.model.DailyGrassDetailStat;
import com.wanted.backend.domain.grass.domain.policy.GrassLearningStatusPolicy;
import com.wanted.backend.domain.grass.domain.repository.DailyGrassDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDailyGrassDetailService implements GetDailyGrassDetailUseCase {

    private final DailyGrassDetailRepository dailyGrassDetailRepository;
    private final GrassLearningStatusPolicy grassLearningStatusPolicy;

    @Override
    public DailyGrassDetailView handle(GetDailyGrassDetailQuery query) {
        DailyGrassDetailStat stat = dailyGrassDetailRepository
                .findByMemberIdAndStatDate(query.memberId(), query.date())
                .orElseGet(() -> DailyGrassDetailStat.empty(query.memberId(), query.date()));

        return new DailyGrassDetailView(
                stat.statDate(),
                stat.watchedLessonCount(),
                stat.studySeconds(),
                grassLearningStatusPolicy.hasStudyRecord(
                        stat.watchedLessonCount(),
                        stat.studySeconds()
                )
        );
    }
}
