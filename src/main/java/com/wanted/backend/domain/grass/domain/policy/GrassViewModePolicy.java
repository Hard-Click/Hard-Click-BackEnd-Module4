package com.wanted.backend.domain.grass.domain.policy;

import com.wanted.backend.domain.grass.domain.model.GrassViewMode;

import java.util.Arrays;

public class GrassViewModePolicy {

    public GrassViewMode resolve(String view) {
        if (view == null || view.isBlank()) {
            throw new IllegalArgumentException("잔디 보기 모드는 필수입니다.");
        }

        return Arrays.stream(GrassViewMode.values())
                .filter(mode -> mode.value().equalsIgnoreCase(view))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잔디 보기 모드는 monthly 또는 yearly여야 합니다."));
    }

    public Integer requireMonthForMonthly(Integer month) {
        if (month == null) {
            throw new IllegalArgumentException("월별 잔디 조회 시 month는 필수입니다.");
        }
        return month;
    }
}
