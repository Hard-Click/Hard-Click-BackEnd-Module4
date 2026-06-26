package com.wanted.backend.domain.study_timer.presentation.api;

import com.wanted.backend.domain.study_timer.application.query.GetDailyStudyTimeQuery;
import com.wanted.backend.domain.study_timer.application.usecase.GetDailyStudyTimeUseCase;
import com.wanted.backend.global.exception.ErrorCode;
import com.wanted.backend.global.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudyTimerStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudyTimerStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDailyStudyTimeUseCase getDailyStudyTimeUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dailyReturnsOkWithDailyStudyTimes() throws Exception {
        authenticateMember(1L);
        when(getDailyStudyTimeUseCase.handle(new GetDailyStudyTimeQuery(
                1L,
                LocalDate.parse("2026-05-01"),
                LocalDate.parse("2026-05-02")
        ))).thenReturn(List.of(
                new GetDailyStudyTimeUseCase.DailyStudyTimeItem(LocalDate.parse("2026-05-01"), 120),
                new GetDailyStudyTimeUseCase.DailyStudyTimeItem(LocalDate.parse("2026-05-02"), 0)
        ));

        mockMvc.perform(get("/api/study-timers/stats/daily")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("일별 순공시간을 조회했습니다."))
                .andExpect(jsonPath("$.data[0].date").value("2026-05-01"))
                .andExpect(jsonPath("$.data[0].studySeconds").value(120))
                .andExpect(jsonPath("$.data[1].date").value("2026-05-02"))
                .andExpect(jsonPath("$.data[1].studySeconds").value(0));
    }

    @Test
    void dailyReturnsStudyTimerErrorWhenStartDateIsMissing() throws Exception {
        authenticateMember(1L);

        mockMvc.perform(get("/api/study-timers/stats/daily")
                        .param("endDate", "2026-05-02"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.details.startDate").value("필수 요청 파라미터입니다."));

        verify(getDailyStudyTimeUseCase, never()).handle(any());
    }

    @Test
    void dailyReturnsStudyTimerErrorWhenEndDateIsMissing() throws Exception {
        authenticateMember(1L);

        mockMvc.perform(get("/api/study-timers/stats/daily")
                        .param("startDate", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.details.endDate").value("필수 요청 파라미터입니다."));

        verify(getDailyStudyTimeUseCase, never()).handle(any());
    }

    @Test
    void dailyReturnsStudyTimerErrorWhenStartDateFormatIsInvalid() throws Exception {
        authenticateMember(1L);

        mockMvc.perform(get("/api/study-timers/stats/daily")
                        .param("startDate", "not-a-date")
                        .param("endDate", "2026-05-02"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));

        verify(getDailyStudyTimeUseCase, never()).handle(any());
    }

    private void authenticateMember(Long memberId) {
        CustomUserDetails userDetails = new CustomUserDetails(
                memberId,
                "test@example.com",
                "password",
                false,
                true,
                "USER",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                )
        );
    }
}
