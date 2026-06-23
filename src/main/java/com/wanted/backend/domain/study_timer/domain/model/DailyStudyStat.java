package com.wanted.backend.domain.study_timer.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDate;

public record DailyStudyStat(
        Long memberId,
        LocalDate studyDate,
        Integer studySeconds
) {

    public DailyStudyStat {
        if (memberId == null || studyDate == null || studySeconds == null || studySeconds < 0) {
            throw new BusinessException(ErrorCode.STUDY_TIMER_DAILY_STAT_INVALID);
        }
    }
}
