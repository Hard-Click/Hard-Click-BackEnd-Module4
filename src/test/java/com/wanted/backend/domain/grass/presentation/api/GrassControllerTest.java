package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.query.GetGrassViewQuery;
import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetGrassViewUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import com.wanted.backend.global.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GrassController.class)
@AutoConfigureMockMvc(addFilters = false)
class GrassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private GetLessonGrassUseCase getLessonGrassUseCase;
    @MockitoBean private GetStudyTimeGrassUseCase getStudyTimeGrassUseCase;
    @MockitoBean private GetMonthlyGrassUseCase getMonthlyGrassUseCase;
    @MockitoBean private GetYearlyGrassUseCase getYearlyGrassUseCase;
    @MockitoBean private GetDailyGrassDetailUseCase getDailyGrassDetailUseCase;
    @MockitoBean private GetStudyStreakUseCase getStudyStreakUseCase;
    @MockitoBean private GetGrassViewUseCase getGrassViewUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void grassReturnsOkWithGrassViewData() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "test@example.com",
                "password",
                false,
                true,
                "USER",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(getGrassViewUseCase.handle(new GetGrassViewQuery(1L, "monthly", 2026, 6)))
                .thenReturn(new GetGrassViewUseCase.GrassView(
                        "monthly",
                        2026,
                        6,
                        List.of(new GetGrassViewUseCase.GrassDayView(
                                LocalDate.parse("2026-06-01"),
                                3,
                                2,
                                false
                        ))
                ));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                )
        );

        mockMvc.perform(get("/api/grass")
                        .param("view", "monthly")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.data.view").value("monthly"))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(6))
                .andExpect(jsonPath("$.data.days[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$.data.days[0].value").value(3))
                .andExpect(jsonPath("$.data.days[0].level").value(2))
                .andExpect(jsonPath("$.data.days[0].isFuture").value(false));
    }

    @Test
    void grassReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/grass")
                        .param("view", "monthly")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }

    @Test
    void grassReturnsBadRequestWhenViewIsInvalid() throws Exception {
        authenticateMember(1L);
        when(getGrassViewUseCase.handle(new GetGrassViewQuery(1L, "weekly", 2026, null)))
                .thenThrow(new IllegalArgumentException("잔디 보기 모드는 monthly 또는 yearly여야 합니다."));

        mockMvc.perform(get("/api/grass")
                        .param("view", "weekly")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    @Test
    void grassReturnsBadRequestWhenMonthIsMissingInMonthlyView() throws Exception {
        authenticateMember(1L);
        when(getGrassViewUseCase.handle(new GetGrassViewQuery(1L, "monthly", 2026, null)))
                .thenThrow(new IllegalArgumentException("월별 잔디 조회 시 month는 필수입니다."));

        mockMvc.perform(get("/api/grass")
                        .param("view", "monthly")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    @Test
    void dailyDetailReturnsBadRequestWhenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/grass/days/not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    @Test
    void dailyDetailReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/grass/days/2026-06-18"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }

    @Test
    void streakReturnsOkWithStreakData() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "test@example.com",
                "password",
                false,
                true,
                "USER",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(getStudyStreakUseCase.handle(new GetStudyStreakQuery(1L)))
                .thenReturn(new GetStudyStreakUseCase.StudyStreakView(5));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                )
        );

        mockMvc.perform(get("/api/grass/streak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.data.streak").value(5));
    }

    @Test
    void streakReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/grass/streak"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
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
