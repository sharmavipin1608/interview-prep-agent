package com.vipinsharma.interviewprep.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchResponse;
import com.vipinsharma.interviewprep.service.ResearchService;

@WebMvcTest({ResearchController.class, GlobalExceptionHandler.class})
class ResearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResearchService researchService;

    private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final CompanyBrief STRIPE_BRIEF = new CompanyBrief(
            "Stripe",
            "Global payment infrastructure company.",
            List.of("Java", "Ruby", "Go"),
            "High ownership culture.",
            "Stripe launched new pricing model.",
            List.of("System design", "Distributed systems"));

    @Test
    void research_returns200WithSessionIdAndCompanyBrief() throws Exception {
        when(researchService.research(anyString(), anyString(), anyString()))
                .thenReturn(new ResearchResponse(SESSION_ID, STRIPE_BRIEF));

        mockMvc.perform(post("/api/v1/research")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Stripe",
                                  "jobTitle": "Software Engineer",
                                  "jobDescription": "Build APIs"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.data.companyBrief.companyName").value("Stripe"));
    }

    @Test
    void research_returns200WithNullError() throws Exception {
        when(researchService.research(anyString(), anyString(), anyString()))
                .thenReturn(new ResearchResponse(SESSION_ID, STRIPE_BRIEF));

        mockMvc.perform(post("/api/v1/research")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Stripe",
                                  "jobTitle": "Software Engineer",
                                  "jobDescription": "Build APIs"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void research_whenServiceThrowsIllegalArgument_returns400WithInvalidRequestCode() throws Exception {
        when(researchService.research(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("companyName must not be null or blank"));

        mockMvc.perform(post("/api/v1/research")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "",
                                  "jobTitle": "Software Engineer",
                                  "jobDescription": "Build APIs"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("companyName must not be null or blank"));
    }
}
