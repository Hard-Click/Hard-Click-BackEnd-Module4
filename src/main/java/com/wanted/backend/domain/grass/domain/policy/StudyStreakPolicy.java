package com.wanted.backend.domain.grass.domain.policy;

import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StudyStreakPolicy {

    private final GrassLearningStatusPolicy grassLearningStatusPolicy;

    public int calculate(LocalDate today, List<StudyStreakStat> stats) {
        if (today == null) {
            throw new IllegalArgumentException("오늘 날짜는 필수입니다.");
        }
        if (stats == null || stats.isEmpty()) {
            return 0;
        }

        Map<LocalDate, StudyStreakStat> statByDate = stats.stream()
                .collect(Collectors.toMap(
                        StudyStreakStat::statDate,
                        stat -> stat,
                        this::merge
                ));

        int streakDays = 0;
        LocalDate currentDate = today;
        while (hasStudyRecord(statByDate.get(currentDate))) {
            streakDays++;
            currentDate = currentDate.minusDays(1);
        }

        return streakDays;
    }

    private boolean hasStudyRecord(StudyStreakStat stat) {
        return stat != null
                && grassLearningStatusPolicy.hasStudyRecord(
                        stat.watchedLessonCount(),
                        stat.studySeconds()
                );
    }

    private StudyStreakStat merge(StudyStreakStat left, StudyStreakStat right) {
        return new StudyStreakStat(
                left.statDate(),
                sum(left.watchedLessonCount(), right.watchedLessonCount()),
                sum(left.studySeconds(), right.studySeconds())
        );
    }

    private int sum(Integer left, Integer right) {
        return valueOrZero(left) + valueOrZero(right);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
