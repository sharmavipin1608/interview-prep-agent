package com.vipinsharma.interviewprep.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.service.ChatService;
import com.vipinsharma.interviewprep.service.InterviewSessionService;
import com.vipinsharma.interviewprep.service.ScoringService;
import com.vipinsharma.interviewprep.service.SessionHistoryService;

@WebMvcTest({SessionController.class, GlobalExceptionHandler.class})
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewSessionService interviewSessionService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ScoringService scoringService;

    @MockBean
    private SessionHistoryService sessionHistoryService;

    private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void startSession_returns201WithSessionId() throws Exception {
        when(interviewSessionService.startSession(any(UUID.class), anyString()))
                .thenReturn(SESSION_ID);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyBriefSessionId": "00000000-0000-0000-0000-000000000002",
                                  "jobDescription": "Build APIs"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(SESSION_ID.toString()));
    }

    @Test
    void chat_returns200WithReply() throws Exception {
        when(chatService.chat(eq(SESSION_ID), anyString()))
                .thenReturn(new ChatResponse("Tell me about system design."));

        mockMvc.perform(post("/api/v1/sessions/{id}/chat", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Hello"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("Tell me about system design."));
    }

    @Test
    void evaluate_returns200WithFeedback() throws Exception {
        SessionFeedback feedback = new SessionFeedback(
                SESSION_ID, 4, List.of("system design"), List.of("practice more"), "Good job overall.");
        when(scoringService.score(eq(SESSION_ID))).thenReturn(feedback);

        mockMvc.perform(post("/api/v1/sessions/{id}/evaluate", SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallScore").value(4))
                .andExpect(jsonPath("$.data.detailedFeedback").value("Good job overall."));
    }

    @Test
    void getSessions_returns200WithList() throws Exception {
        SessionSummary summary = new SessionSummary(
                SESSION_ID, "Stripe", "Software Engineer", "ACTIVE", null, LocalDateTime.now());
        when(sessionHistoryService.getSessions()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].companyName").value("Stripe"))
                .andExpect(jsonPath("$.data[0].jobTitle").value("Software Engineer"));
    }

    @Test
    void getSession_whenFound_returns200() throws Exception {
        SessionSummary summary = new SessionSummary(
                SESSION_ID, "Stripe", "Software Engineer", "ACTIVE", 4, LocalDateTime.now());
        when(sessionHistoryService.getSessions()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/sessions/{id}", SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.data.overallScore").value(4));
    }

    @Test
    void getSession_whenNotFound_returns400() throws Exception {
        when(sessionHistoryService.getSessions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/sessions/{id}", SESSION_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void startSession_whenServiceThrowsIllegalArgument_returns400() throws Exception {
        when(interviewSessionService.startSession(any(UUID.class), anyString()))
                .thenThrow(new IllegalArgumentException("Session not found: " + SESSION_ID));

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyBriefSessionId": "00000000-0000-0000-0000-000000000002",
                                  "jobDescription": "Build APIs"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void getWeakAreas_returnsList() throws Exception {
        when(sessionHistoryService.getTopWeakAreas())
                .thenReturn(List.of("System Design", "Concurrency"));

        mockMvc.perform(get("/api/v1/sessions/weak-areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("System Design"))
                .andExpect(jsonPath("$.data[1]").value("Concurrency"));
    }
}
