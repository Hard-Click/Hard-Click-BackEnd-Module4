package com.wanted.backend.domain.study_timer.application.service;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase;
import com.wanted.backend.domain.study_timer.domain.model.DailyStudyStat;
import com.wanted.backend.domain.study_timer.domain.repository.DailyStudyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDailyStudyTimeService implements GetDailyStudyTimeUseCase {

    private final DailyStudyStatsRepository dailyStudyStatsRepository;

    @Override
    public List<DailyStudyTimeItem> handle(GetDailyStudyTimeQuery query) {
        validate(query);

        List<DailyStudyStat> stats = dailyStudyStatsRepository.findByMemberIdAndDateBetween(
                query.memberId(),
                query.startDate(),
                query.endDate()
        );

        Map<LocalDate, Integer> studySecondsByDate = stats.stream()
                .collect(Collectors.toMap(
                        DailyStudyStat::studyDate,
                        DailyStudyStat::studySeconds,
                        Integer::sum
                ));

        return query.startDate()
                .datesUntil(query.endDate().plusDays(1))
                .map(date -> new DailyStudyTimeItem(
                        date,
                        studySecondsByDate.getOrDefault(date, 0)
                ))
                .toList();
    }

    private void validate(GetDailyStudyTimeQuery query) {
        if (query.memberId() == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (query.startDate() == null) {
            throw new IllegalArgumentException("시작 날짜는 필수입니다.");
        }
        if (query.endDate() == null) {
            throw new IllegalArgumentException("종료 날짜는 필수입니다.");
        }
        if (query.startDate().isAfter(query.endDate())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜 이후일 수 없습니다.");
        }
    }
}
