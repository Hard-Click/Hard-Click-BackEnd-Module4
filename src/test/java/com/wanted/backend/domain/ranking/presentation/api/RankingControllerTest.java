package com.wanted.backend.domain.ranking.presentation.api;

import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.query.GetStudyTimeRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase;
import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase;
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

    @MockitoBean
    private GetStudyTimeRankingUseCase getStudyTimeRankingUseCase;

    @MockitoBean
    private GetLessonRankingUseCase getLessonRankingUseCase;

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
    void studyTimeReturnsOkWithRankingList() throws Exception {
        authenticateMember(1L);
        when(getStudyTimeRankingUseCase.handle(new GetStudyTimeRankingQuery("daily")))
                .thenReturn(new GetStudyTimeRankingUseCase.StudyTimeRankingView(
                        "daily",
                        2L,
                        List.of(
                                new GetStudyTimeRankingUseCase.StudyTimeRankingItem(1L, 1L, 7200L),
                                new GetStudyTimeRankingUseCase.StudyTimeRankingItem(2L, 2L, 3600L)
                        )
                ));

        mockMvc.perform(get("/api/rankings/study-time")
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("순공시간 랭킹을 조회했습니다."))
                .andExpect(jsonPath("$.data.period").value("daily"))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.rankings[0].memberId").value(1))
                .andExpect(jsonPath("$.data.rankings[0].studySeconds").value(7200))
                .andExpect(jsonPath("$.data.rankings[1].rank").value(2))
                .andExpect(jsonPath("$.data.rankings[1].memberId").value(2))
                .andExpect(jsonPath("$.data.rankings[1].studySeconds").value(3600));
    }

    @Test
    void studyTimeReturnsMonthlyRankingWhenPeriodIsOmitted() throws Exception {
        authenticateMember(1L);
        when(getStudyTimeRankingUseCase.handle(new GetStudyTimeRankingQuery(null)))
                .thenReturn(new GetStudyTimeRankingUseCase.StudyTimeRankingView(
                        "monthly",
                        0L,
                        List.of()
                ));

        mockMvc.perform(get("/api/rankings/study-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.data.period").value("monthly"))
                .andExpect(jsonPath("$.data.totalUsers").value(0))
                .andExpect(jsonPath("$.data.rankings").isEmpty());
    }

    @Test
    void studyTimeReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rankings/study-time"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }

    @Test
    void studyTimeReturnsBadRequestWhenPeriodIsInvalid() throws Exception {
        authenticateMember(1L);
        when(getStudyTimeRankingUseCase.handle(new GetStudyTimeRankingQuery("yearly")))
                .thenThrow(new IllegalArgumentException("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다."));

        mockMvc.perform(get("/api/rankings/study-time")
                        .param("period", "yearly"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
    }

    @Test
    void lessonsReturnsOkWithRankingList() throws Exception {
        authenticateMember(1L);
        when(getLessonRankingUseCase.handle(new GetLessonRankingQuery("daily")))
                .thenReturn(new GetLessonRankingUseCase.LessonRankingView(
                        "daily",
                        2L,
                        List.of(
                                new GetLessonRankingUseCase.LessonRankingItem(1L, 1L, 12L),
                                new GetLessonRankingUseCase.LessonRankingItem(2L, 2L, 8L)
                        )
                ));

        mockMvc.perform(get("/api/rankings/lessons")
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("수강량 랭킹을 조회했습니다."))
                .andExpect(jsonPath("$.data.period").value("daily"))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.rankings[0].memberId").value(1))
                .andExpect(jsonPath("$.data.rankings[0].watchedLessonCount").value(12))
                .andExpect(jsonPath("$.data.rankings[1].rank").value(2))
                .andExpect(jsonPath("$.data.rankings[1].memberId").value(2))
                .andExpect(jsonPath("$.data.rankings[1].watchedLessonCount").value(8));
    }

    @Test
    void lessonsReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/rankings/lessons"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }

    @Test
    void lessonsReturnsBadRequestWhenPeriodIsInvalid() throws Exception {
        authenticateMember(1L);
        when(getLessonRankingUseCase.handle(new GetLessonRankingQuery("yearly")))
                .thenThrow(new IllegalArgumentException("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다."));

        mockMvc.perform(get("/api/rankings/lessons")
                        .param("period", "yearly"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C001"));
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
