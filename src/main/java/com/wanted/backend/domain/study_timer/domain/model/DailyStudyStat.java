package com.wanted.backend.domain.study_timer.domain.model;

import java.time.LocalDate;

public record DailyStudyStat(
        Long memberId,
        LocalDate studyDate,
        Integer studySeconds
) {

    public DailyStudyStat {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (studyDate == null) {
            throw new IllegalArgumentException("학습 날짜는 필수입니다.");
        }
        if (studySeconds == null) {
            throw new IllegalArgumentException("순공시간은 필수입니다.");
        }
        if (studySeconds < 0) {
            throw new IllegalArgumentException("순공시간은 0 이상이어야 합니다.");
        }
    }
}
