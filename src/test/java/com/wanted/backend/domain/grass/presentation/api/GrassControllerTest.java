package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.query.GetStudyStreakQuery;
import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
}
