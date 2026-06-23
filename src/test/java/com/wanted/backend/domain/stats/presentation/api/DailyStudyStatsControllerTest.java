package com.wanted.backend.domain.stats.presentation.api;

import com.wanted.backend.domain.stats.application.query.GetDailyStudyStatQuery;
import com.wanted.backend.domain.stats.application.usecase.GetDailyStudyStatUseCase;
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

@WebMvcTest(controllers = DailyStudyStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class DailyStudyStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDailyStudyStatUseCase getDailyStudyStatUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dailyStudyReturnsOkWithDailyStudyStat() throws Exception {
        authenticateMember(1L);
        LocalDate date = LocalDate.parse("2026-06-18");
        when(getDailyStudyStatUseCase.handle(new GetDailyStudyStatQuery(1L, date)))
                .thenReturn(new GetDailyStudyStatUseCase.DailyStudyStatView(
                        date,
                        3,
                        9000,
                        2
                ));

        mockMvc.perform(get("/api/stats/daily-study/2026-06-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("특정 날짜 학습 통계를 조회했습니다."))
                .andExpect(jsonPath("$.data.date").value("2026-06-18"))
                .andExpect(jsonPath("$.data.watchedLessonCount").value(3))
                .andExpect(jsonPath("$.data.studySeconds").value(9000))
                .andExpect(jsonPath("$.data.completedLessonCount").value(2));
    }

    @Test
    void dailyStudyReturnsStatsErrorWhenDateFormatIsInvalid() throws Exception {
        authenticateMember(1L);

        mockMvc.perform(get("/api/stats/daily-study/not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.DAILY_STATS_DATE_FORMAT_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DAILY_STATS_DATE_FORMAT_INVALID.getMessage()));

        verify(getDailyStudyStatUseCase, never()).handle(any());
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
