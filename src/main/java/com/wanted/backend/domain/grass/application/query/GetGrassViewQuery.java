package com.wanted.backend.domain.grass.application.query;

public record GetGrassViewQuery(
        Long memberId,
        String view,
        Integer year,
        Integer month
) {
}
