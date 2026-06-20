package com.wanted.backend.domain.ranking.presentation.api;

import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RankingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetMyRankingSummaryUseCase getMyRankingSummaryUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void meReturnsOkWithRankingSummary() throws Exception {
        authenticateMember(1L);
        when(getMyRankingSummaryUseCase.handle(new GetMyRankingSummaryQuery(1L, "study-time", "monthly")))
                .thenReturn(new GetMyRankingSummaryUseCase.MyRankingSummaryView(
                        "study-time",
                        "monthly",
                        12L,
                        200L,
                        6.0
                ));

        mockMvc.perform(get("/api/rankings/me")
                        .param("metric", "study-time")
                        .param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("내 랭킹 상세 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data.metric").value("study-time"))
                .andExpect(jsonPath("$.data.period").value("monthly"))
                .andExpect(jsonPath("$.data.rank").value(12))
                .andExpect(jsonPath("$.data.totalUsers").value(200))
                .andExpect(jsonPath("$.data.topPercent").value(6.0));
    }

    @Test
    void meReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rankings/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }

    @Test
    void meReturnsBadRequestWhenMetricIsInvalid() throws Exception {
        authenticateMember(1L);
        when(getMyRankingSummaryUseCase.handle(new GetMyRankingSummaryQuery(1L, "likes", null)))
                .thenThrow(new IllegalArgumentException("랭킹 기준은 study-time, lessons, accepted-comments 중 하나여야 합니다."));

        mockMvc.perform(get("/api/rankings/me")
                        .param("metric", "likes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    @Test
    void meReturnsBadRequestWhenPeriodIsInvalid() throws Exception {
        authenticateMember(1L);
        when(getMyRankingSummaryUseCase.handle(new GetMyRankingSummaryQuery(1L, "study-time", "yearly")))
                .thenThrow(new IllegalArgumentException("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다."));

        mockMvc.perform(get("/api/rankings/me")
                        .param("metric", "study-time")
                        .param("period", "yearly"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
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
