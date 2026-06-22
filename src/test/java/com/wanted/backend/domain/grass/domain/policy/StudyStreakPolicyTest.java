package com.wanted.backend.domain.grass.domain.policy;

import com.wanted.backend.domain.grass.domain.model.StudyStreakStat;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudyStreakPolicyTest {

    private final StudyStreakPolicy policy = new StudyStreakPolicy(new GrassLearningStatusPolicy());

    @Test
    void calculatesConsecutiveStudyDaysFromToday() {
        int result = policy.calculate(
                LocalDate.parse("2026-06-20"),
                List.of(
                        new StudyStreakStat(LocalDate.parse("2026-06-20"), 1, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-19"), 0, 3600),
                        new StudyStreakStat(LocalDate.parse("2026-06-18"), 2, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-16"), 10, 0)
                )
        );

        assertThat(result).isEqualTo(3);
    }

    @Test
    void returnsZeroWhenTodayHasNoStudyRecord() {
        int result = policy.calculate(
                LocalDate.parse("2026-06-20"),
                List.of(
                        new StudyStreakStat(LocalDate.parse("2026-06-19"), 1, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-18"), 1, 0)
                )
        );

        assertThat(result).isZero();
    }

    @Test
    void stopsWhenZeroStatExistsBetweenStudyDays() {
        int result = policy.calculate(
                LocalDate.parse("2026-06-20"),
                List.of(
                        new StudyStreakStat(LocalDate.parse("2026-06-20"), 1, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-19"), 0, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-18"), 1, 0)
                )
        );

        assertThat(result).isEqualTo(1);
    }

    @Test
    void mergesDuplicatedDateRowsDefensively() {
        int result = policy.calculate(
                LocalDate.parse("2026-06-20"),
                List.of(
                        new StudyStreakStat(LocalDate.parse("2026-06-20"), 0, 0),
                        new StudyStreakStat(LocalDate.parse("2026-06-20"), 0, 1)
                )
        );

        assertThat(result).isEqualTo(1);
    }
}
