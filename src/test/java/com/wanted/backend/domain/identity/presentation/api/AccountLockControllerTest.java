package com.wanted.backend.domain.identity.presentation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.backend.domain.identity.application.usecase.AccountLockUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountLockController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountLockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountLockUseCase accountLockUseCase;

    @Test
    @DisplayName("계정 잠금 인증번호 발송 성공")
    void sendAccountLockCode_success() throws Exception {
        String email = "user@example.com";

        String body = """
                {
                    "email": "%s"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/account-locks/email")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("계정 보호 인증번호가 발송되었습니다"))
                .andExpect(jsonPath("$.data").isMap());

        verify(accountLockUseCase).sendCode(email);
    }
}