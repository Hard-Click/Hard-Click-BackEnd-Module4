package com.wanted.backend.domain.grass.presentation.api;

import com.wanted.backend.domain.grass.application.usecase.GetDailyGrassDetailUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetLessonGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetMonthlyGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyStreakUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetStudyTimeGrassUseCase;
import com.wanted.backend.domain.grass.application.usecase.GetYearlyGrassUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

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
}
